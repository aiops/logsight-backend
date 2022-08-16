package ai.logsight.backend

import ai.logsight.backend.incidents.domain.Incident
import ai.logsight.backend.incidents.domain.dto.IncidentDTO
import ai.logsight.backend.incidents.domain.dto.IncidentGroupDTO
import ai.logsight.backend.incidents.extensions.toIncidentMessage
import ai.logsight.backend.incidents.extensions.toIncidentMessageDTO
import ai.logsight.backend.incidents.ports.out.persistence.elasticsearch.entities.ESIncidentMessage
import ai.logsight.backend.logs.domain.LogBatch
import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.extensions.toLogBatchDTO
import ai.logsight.backend.logs.ingestion.domain.LogReceipt
import ai.logsight.backend.logs.ingestion.domain.enums.LogBatchStatus
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogMessage
import ai.logsight.backend.logs.ingestion.ports.web.responses.LogReceiptResponse
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

    val baseUserEntity = UserEntity(
        email = baseEmail,
        password = passwordEncoder.encode(basePassword),
        userType = UserType.ONLINE_USER,
        activated = true
    )
    val baseUser = baseUserEntity.toUser()

    // Logs
    val defaultTag = mapOf("default" to "default")

    val sendLogMessage = SendLogMessage(
        message = "Hello World!",
        timestamp = DateTime.now()
            .toString(),
        level = "INFO",
        tags = defaultTag
    )
    val logsightLog = LogsightLog(
        message = "Hello World!",
        timestamp = DateTime.now()
            .toString(),
        level = "INFO", tags = defaultTag
    )
    const val numMessages = 100
    val logBatch = LogBatch(
        index = baseUser.key,
        logs = List(numMessages) { logsightLog }
    )
    val logBatchDTO = logBatch.toLogBatchDTO()
    val logReceipt = LogReceipt(
        id = UUID.randomUUID(),
        logsCount = logBatch.logs.size,
        batchId = logBatch.id,
        processedLogCount = 0,
        status = LogBatchStatus.PROCESSING
    )
    val logReceiptResponse = LogReceiptResponse(
        logsCount = logReceipt.logsCount,
        receiptId = logReceipt.id,
        status = logReceipt.status,
        batchId = logReceipt.batchId
    )

    val incidentId = "exampleIncidentId"
    val esIncidentMessage = ESIncidentMessage(
        "timestamp", "template", "level", 0.0, "message",
        defaultTag, 0, 0, 0, ""
    )
    val incidentMessage = esIncidentMessage.toIncidentMessage()
    val incidentMessageDTO = incidentMessage.toIncidentMessageDTO()
    val incident = Incident(
        incidentId, "timestamp", 0, 0, 0, 1, 0,
        0, 1, defaultTag, 0, message = incidentMessage, data = listOf(incidentMessage)
    )
    val incidentDTO = IncidentDTO(
        incidentId, "timestamp", 0, 0, 0, 1, 0,
        0, 1, defaultTag, 0, message = incidentMessageDTO
    )
    val incidentGroupSize = 2
    val incidentGroupDTO = IncidentGroupDTO(
        head = incidentDTO,
        incidents = List(incidentGroupSize) { incidentDTO }
    )
}
