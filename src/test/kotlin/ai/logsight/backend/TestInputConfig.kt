package ai.logsight.backend

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.extensions.toApplication
import ai.logsight.backend.application.ports.out.persistence.ApplicationEntity
import ai.logsight.backend.logs.domain.LogBatch
import ai.logsight.backend.logs.domain.LogEvent
import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.extensions.toLogBatchDTO
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogEventsDTO
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogMessage
import ai.logsight.backend.users.extensions.toUser
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import ai.logsight.backend.users.ports.out.persistence.UserType
import org.joda.time.DateTime
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

object TestInputConfig {

    val passwordEncoder = BCryptPasswordEncoder()

    const val baseEmail = "testemail@gmail.com"
    const val basePassword = "testpassword"
    const val baseAppName = "test_app"

    val baseUserEntity = UserEntity(
        email = baseEmail,
        password = passwordEncoder.encode(basePassword),
        userType = UserType.ONLINE_USER,
        activated = true
    )
    val baseUser = baseUserEntity.toUser()

    val baseAppEntityReady = ApplicationEntity(
        name = baseAppName, user = baseUserEntity, status = ApplicationStatus.READY,
        index = baseAppName
    )
    val baseApp = baseAppEntityReady.toApplication()
    fun getAppWithStatus(status: ApplicationStatus): Application {
        return baseApp.copy(status = status)
    }

    // Logs
    val defaultTag = mapOf("default" to "default")
    val logEvent = LogEvent(
        message = "Hello World!",
        timestamp = DateTime.now()
            .toString(),
        level = "INFO"
    )
    val createApplicationCommand = CreateApplicationCommand(
        applicationName = baseAppName,
        user = baseUser,
        displayName = baseAppName,
    )
    val sendLogMessageByAppName = SendLogMessage(
        applicationName = baseAppName,
        timestamp = logEvent.timestamp,
        message = logEvent.message,
        level = logEvent.level,
        tags = defaultTag
    )
    val sendLogMessageByAppId = SendLogMessage(
        applicationId = baseApp.id,
        timestamp = logEvent.timestamp,
        message = logEvent.message,
        level = logEvent.level,
        tags = defaultTag
    )

    val sendLogMessage = SendLogMessage(
        message = logEvent.message,
        timestamp = logEvent.timestamp,
        applicationId = baseApp.id,
        level = logEvent.level,
        tags = defaultTag
    )
    val logsightLog = LogsightLog(
        event = logEvent, tags = defaultTag
    )
    const val numMessages = 100
    val logBatch = LogBatch(
        application = baseApp,
        logs = List(numMessages) { logsightLog }
    )
    val logBatchDTO = logBatch.toLogBatchDTO()
    val logsReceipt = LogsReceipt(UUID.randomUUID(), orderNum = 1, numMessages, baseApp)
    fun getLogsReceipts(num: Int = 1): List<LogsReceipt> {
        return (1..num).map { logsReceipt.copy(orderNum = it.toLong()) }
    }

    val logEventsDTOById = LogEventsDTO(
        user = baseUser,
        logs = List(numMessages) { sendLogMessageByAppId }
    )
    val logEventsDTOByName = LogEventsDTO(
        user = baseUser,
        logs = List(numMessages) { sendLogMessageByAppName }
    )
    val logEventsDTOMixed = LogEventsDTO(
        user = baseUser,
        logs = List(numMessages) { sendLogMessageByAppId } + List(numMessages) { sendLogMessageByAppName }
    )
}
