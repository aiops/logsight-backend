package ai.logsight.backend.autolog.domain.service

import ai.logsight.backend.autolog.domain.dto.AutoLogDTO
import ai.logsight.backend.autolog.exceptions.RemoteAutoLogException
import ai.logsight.backend.autolog.ports.web.out.persistance.AutoLogEntity
import ai.logsight.backend.autolog.ports.web.out.persistance.AutoLogRepository
import ai.logsight.backend.autolog.ports.web.response.AutoLogEntry
import ai.logsight.backend.autolog.ports.web.response.AutoLogResponse
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.compare.ports.out.HttpClientFactory
import ai.logsight.backend.compare.ports.out.config.ResultAPIRESTConfigProperties
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.extensions.toUserEntity
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

@Service
class AutoLogService (
    private val restConfigProperties: ResultAPIRESTConfigProperties,
    private val httpClientFactory: HttpClientFactory,
    private val autoLogRepository: AutoLogRepository

)  {
    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    private val logger = LoggerImpl(AutoLogService::class.java)

    fun getAutoLogs(autoLogDTO: AutoLogDTO): AutoLogResponse {
        val uri = buildAutoLogEndpointURI()
        val requestBody = mapOf(
            autoLogDTO::code.name to autoLogDTO.code,
            autoLogDTO::language.name to autoLogDTO.language,
        )
        val autoLog =
            autoLogRepository.save(AutoLogEntity(language = autoLogDTO.language, user = autoLogDTO.user.toUserEntity()))

        val request = HttpRequest.newBuilder().uri(uri).header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody))).build()
        val response = httpClientFactory.create().send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != HttpStatus.OK.value()) throw RemoteAutoLogException(
            response.body().toString()
        )
        val autoLogEntities = autoLogRepository.findAllByUser(autoLog.user)
        val shouldShowFeedback = (autoLogEntities.size % ThreadLocalRandom.current().nextInt(5, 10)) == 0
        val autoLogs = mapper.readValue<List<AutoLogEntry>>(response.body().toString())
        return AutoLogResponse(
            listAutoLogs = autoLogs,
            autoLogId = autoLog.id,
            shouldShowFeedback = shouldShowFeedback
        )

    }

    fun giveFeedback(user: User, autoLogId: UUID, isHelpful: Boolean): AutoLogEntity {
        val autoLogEntity = autoLogRepository.getById(autoLogId)
        return autoLogRepository.save(AutoLogEntity(id = autoLogId, isHelpful = isHelpful, language = autoLogEntity.language, user = autoLogEntity.user))
    }

    private fun buildAutoLogEndpointURI() =
        UriComponentsBuilder.newInstance().scheme(restConfigProperties.scheme).host(restConfigProperties.host)
            .port(restConfigProperties.port).path(restConfigProperties.autologPath).build().toUri()

}
