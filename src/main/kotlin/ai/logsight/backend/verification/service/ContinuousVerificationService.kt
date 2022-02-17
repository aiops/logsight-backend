package ai.logsight.backend.verification.service

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.service.ChartsService
import ai.logsight.backend.charts.repository.ESChartRepository
import ai.logsight.backend.charts.rest.ChartsController
import ai.logsight.backend.charts.rest.request.ChartRequest
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.connectors.rest.RestTemplateConnector
import ai.logsight.backend.results.domain.service.ResultInitStatus
import ai.logsight.backend.results.exceptions.ResultInitAlreadyPendingException
import ai.logsight.backend.results.ports.persistence.ResultInitStorageService
import ai.logsight.backend.verification.dto.VerificationDTO
import ai.logsight.backend.verification.exceptions.RemoteVerificationException
import ai.logsight.backend.verification.out.rest.config.VerificationRESTConfigProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

@Service
class ContinuousVerificationService(
    private val restConfigProperties: VerificationRESTConfigProperties,
    private val applicationStorageService: ApplicationStorageService,
    private val chartsRepository: ESChartRepository,
    private val resultInitStorageService: ResultInitStorageService,
    private val chartsService: ChartsService
) {
    val resetTemplate = RestTemplateConnector()
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .build()
    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    private val logger = LoggerImpl(ChartsController::class.java)

    fun getVerificationData(verificationDTO: VerificationDTO): String {
        try {
            val resultInit = verificationDTO.resultInitId?.let { resultInitStorageService.findResultInitById(it) }
            if (resultInit!!.status != ResultInitStatus.DONE) {
                throw ResultInitAlreadyPendingException("Result init is not yet ready. Please try again later, initiate new result, or send a request without an ID to force getting results.")
            }
        } catch (e: Exception) {
            logger.warn("Result init is null. Getting results anyway!")
        }

        val uri = buildVerificationEndpointURI(verificationDTO)
        val request =
            HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != HttpStatus.OK.value())
            throw RemoteVerificationException(
                response.body()
                    .toString()
            )
        return response.body()
            .toString()
    }

    fun getVerificationTags(userId: UUID, applicationId: UUID): MutableList<String> {
        val application = applicationStorageService.findApplicationById(applicationId)
        val applicationIndex = "${application.user.key}_${application.name}_log_ad"
        val chartRequest = ChartRequest(applicationId = applicationId, chartConfig = ChartConfig("util", "now", "now", "versions", "log_ad"))
        val getChartDataQuery = chartsService.getChartQuery(application.user.id, chartRequest)
        val tags = chartsRepository.getData(getChartDataQuery, applicationIndex) // use mapper here
        val dataList = mutableListOf<String>()
        JSONObject(tags)
            .getJSONObject("aggregations")
            .getJSONObject("listAggregations")
            .getJSONArray("buckets")
            .forEach {
                dataList.add(JSONObject(it.toString()).getString("key"))
            }
        return dataList
    }

    private fun buildVerificationEndpointURI(verificationDTO: VerificationDTO) =
        UriComponentsBuilder.newInstance()
            .scheme("http")
            .host(restConfigProperties.host)
            .port(restConfigProperties.port)
            .path("/api")
            .path("/v${restConfigProperties.apiVersion}")
            .path("/${restConfigProperties.endpoint}")
            .queryParam(VerificationDTO::applicationId.name, verificationDTO.applicationId)
            .queryParam(VerificationDTO::applicationName.name, verificationDTO.applicationName)
            .queryParam(VerificationDTO::baselineTag.name, verificationDTO.baselineTag)
            .queryParam(VerificationDTO::compareTag.name, verificationDTO.compareTag)
            .queryParam(VerificationDTO::privateKey.name, verificationDTO.privateKey)
            .build()
            .toUri()
}
