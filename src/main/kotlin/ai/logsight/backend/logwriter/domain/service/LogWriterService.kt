package ai.logsight.backend.logwriter.domain.service

import ai.logsight.backend.logwriter.domain.dto.LogWriterDTO
import ai.logsight.backend.logwriter.exceptions.RemoteLogWriterException
import ai.logsight.backend.logwriter.ports.web.out.persistance.LogWriterEntity
import ai.logsight.backend.logwriter.ports.web.out.persistance.LogWriterRepository
import ai.logsight.backend.logwriter.ports.web.response.LogWriterEntry
import ai.logsight.backend.logwriter.ports.web.response.LogWriterResponse
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
class LogWriterService (
    private val restConfigProperties: ResultAPIRESTConfigProperties,
    private val httpClientFactory: HttpClientFactory,
    private val logWriterRepository: LogWriterRepository

)  {
    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    private val logger = LoggerImpl(LogWriterService::class.java)

    fun getLogWriterLogs(logWriterDTO: LogWriterDTO): LogWriterResponse {
        val uri = buildLogWriterEndpointURI()
        val requestBody = mapOf(
            logWriterDTO::code.name to logWriterDTO.code,
            logWriterDTO::language.name to logWriterDTO.language,
        )
        val logWrite =
            logWriterRepository
                .save(LogWriterEntity(language = logWriterDTO.language, user = logWriterDTO.user.toUserEntity()))

        val request = HttpRequest.newBuilder().uri(uri).header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody))).build()
        val response = httpClientFactory.create().send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != HttpStatus.OK.value()) throw RemoteLogWriterException(
            response.body().toString()
        )
        val logWriteEntities = logWriterRepository.findAllByUser(logWrite.user)
        val shouldShowFeedback = (logWriteEntities.size % ThreadLocalRandom.current().nextInt(5, 6)) == 0
        val logWriterLogs = mapper.readValue<List<LogWriterEntry>>(response.body().toString())
        return LogWriterResponse(
            listWriteLogs = logWriterLogs,
            logWriterId = logWrite.id,
            shouldShowFeedback = shouldShowFeedback
        )

    }

    fun giveFeedback(user: User, autoLogId: UUID, isHelpful: Boolean): LogWriterEntity {
        val autoLogEntity = logWriterRepository.getById(autoLogId)
        return logWriterRepository.save(LogWriterEntity(id = autoLogId, isHelpful = isHelpful, language = autoLogEntity.language, user = autoLogEntity.user))
    }

    private fun buildLogWriterEndpointURI() =
        UriComponentsBuilder.newInstance().scheme(restConfigProperties.scheme).host(restConfigProperties.host)
            .port(restConfigProperties.port).path(restConfigProperties.logWriterPath).build().toUri()

}
