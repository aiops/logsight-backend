package ai.logsight.backend.compare.domain.service

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.charts.domain.service.ChartsService
import ai.logsight.backend.charts.ports.web.ChartsController
import ai.logsight.backend.charts.repository.ESChartRepository
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.compare.domain.dto.CompareDTO
import ai.logsight.backend.compare.exceptions.RemoteCompareException
import ai.logsight.backend.compare.ports.out.config.CompareRESTConfigProperties
import ai.logsight.backend.compare.ports.web.response.CompareDataResponse
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class CompareService(
    private val restConfigProperties: CompareRESTConfigProperties,
    private val logsReceiptStorageService: LogsReceiptStorageService,
) {
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .build()
    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    private val logger = LoggerImpl(ChartsController::class.java)

    // this contains also the table with the states/templates
    fun getCompareDataView(compareDTO: CompareDTO): String {
        val uri = buildCompareEndpointURI()
        val requestBody = mapOf(
            CompareDTO::baselineTags.name to JSONObject(compareDTO.baselineTags).toString(),
            CompareDTO::candidateTags.name to JSONObject(compareDTO.candidateTags).toString(),
            CompareDTO::privateKey.name to compareDTO.privateKey
        )
        val request =
            HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != HttpStatus.OK.value())
            throw RemoteCompareException(
                response.body()
                    .toString()
            )
        return response.body().toString()
    }

    fun getCompareData(compareDTO: CompareDTO): CompareDataResponse {
        val logsReceipt = compareDTO.logsReceiptId?.let {
            logsReceiptStorageService.findLogsReceiptById(it)
        }
        return mapper.readValue<CompareDataResponse>(getCompareDataView(compareDTO))
    }

    private fun buildCompareEndpointURI() =
        UriComponentsBuilder.newInstance()
            .scheme(restConfigProperties.scheme)
            .host(restConfigProperties.host)
            .port(restConfigProperties.port)
            .path(restConfigProperties.comparePath)
            .build()
            .toUri()
}
