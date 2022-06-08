package ai.logsight.backend

import ai.logsight.backend.logs.domain.LogBatch
import ai.logsight.backend.logs.domain.LogEvent
import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.extensions.toLogBatchDTO
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogMessage
import ai.logsight.backend.users.extensions.toUser
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import ai.logsight.backend.users.ports.out.persistence.UserType
import org.joda.time.DateTime
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

object TestInputConfig {

    val passwordEncoder = BCryptPasswordEncoder()

    const val baseEmail = "testemail@gmail.com"
    const val basePassword = "testpassword"

    val baseUserEntity = UserEntity(
        email = baseEmail,
        password = passwordEncoder.encode(basePassword),
        userType = UserType.ONLINE_USER,
        activated = true
    )
    val baseUser = baseUserEntity.toUser()

    // Logs
    val defaultTag = mapOf("default" to "default")
    val logEvent = LogEvent(
        message = "Hello World!",
        timestamp = DateTime.now()
            .toString(),
        level = "INFO"
    )

    val sendLogMessage = SendLogMessage(
        message = logEvent.message,
        timestamp = logEvent.timestamp,
        level = logEvent.level,
        tags = defaultTag
    )
    val logsightLog = LogsightLog(
        event = logEvent, tags = defaultTag
    )
    const val numMessages = 100
    val logBatch = LogBatch(
        index = baseUser.key,
        logs = List(numMessages) { logsightLog }
    )
    val logBatchDTO = logBatch.toLogBatchDTO()
}
