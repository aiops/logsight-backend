package ai.logsight.backend.results.domain.service

import ai.logsight.backend.common.logging.Logger
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.common.utils.TopicBuilder
import ai.logsight.backend.results.domain.ResultInit
import ai.logsight.backend.results.domain.service.command.CreateResultInitCommand
import ai.logsight.backend.results.domain.service.command.UpdateResultInitStatusCommand
import ai.logsight.backend.results.exceptions.ResultInitAlreadyPendingException
import ai.logsight.backend.results.ports.persistence.ResultInitStorageService
import ai.logsight.backend.results.ports.rpc.ResultInitRPCService
import ai.logsight.backend.results.ports.rpc.dto.FlushDTO
import ai.logsight.backend.results.ports.rpc.dto.FlushDTOOperations
import com.antkorwin.xsync.XSync
import org.springframework.stereotype.Service

@Service
class ResultServiceImpl(
    val resultInitStorageService: ResultInitStorageService,
    val resultInitRPCService: ResultInitRPCService,
    val topicBuilder: TopicBuilder,
    val xSync: XSync<String>
) : ResultService {

    private val logger: Logger = LoggerImpl(ResultServiceImpl::class.java)

    // TODO make configurable
    private var topicPostfix: String = "input_ctrl"

    override fun createResultInit(createResultInitCommand: CreateResultInitCommand): ResultInit {
        // This is needed due to the mutex block. Later it's ugly with ?.let There might be a better way?
        var resultInit: ResultInit? = null
        // MUTEX to prevent duplicated ResultInit objects with status PENDING
        xSync.execute("result init") {
            // try to load ResultInit where status is in PENDING
            val results = resultInitStorageService.findAllResultInitByStatusAndApplicationId(
                ResultInitStatus.PENDING,
                createResultInitCommand.logsReceipt.application.id
            )
            resultInit = when (results.size) {
                // none pending -> it is allowed to create a new ResultInit object and store it in DB
                0 -> resultInitStorageService.saveResultInit(createResultInitCommand)
                // pending -> throw exception since only one at a time is allowed
                1 -> throw ResultInitAlreadyPendingException(
                    "A result init ${results[0]} is already pending. Try to poll for the results."
                )
                // TODO: This invalid state can be fixed. Write a routine to terminate the ResulInit request to restore a valid state
                else -> throw ResultInitAlreadyPendingException(
                    "Invalid state. There are ${results.size} pending result inits but 1 is allowed."
                )
            }
        }

        return resultInit?.let { resultInitNotNull ->
            // Create DTO to transfer to logsight core
            val flushDTO = FlushDTO(
                id = resultInitNotNull.id,
                orderNum = resultInitNotNull.logsReceipt.orderNum,
                logsCount = resultInitNotNull.logsReceipt.logsCount,
                operation = FlushDTOOperations.FLUSH
            )
            // create topic
            val topic = topicBuilder.buildTopic(
                listOf(createResultInitCommand.user.key, createResultInitCommand.application.name, topicPostfix)
            )
            // Send flush command to logsight core
            try {
                resultInitRPCService.flush(topic, flushDTO)
            } catch (e: Exception) {
                // Revert DB entry if sending failed
                logger.error(
                    "Failed to send flush RPC $flushDTO. Reason: ${e.message}",
                    this::updateResultInitStatus.name
                )
                resultInitStorageService.deleteResultInit(resultInitNotNull)
                throw RuntimeException("Failed to send flush RPC $flushDTO. Reason: ${e.message}")
            }
            resultInitNotNull
        } ?: throw RuntimeException() // This should not happen
    }

    override fun updateResultInitStatus(updateResultInitStatusCommand: UpdateResultInitStatusCommand): ResultInit? {
        fun logError(e: Exception) = logger.error(
            "Failed to update status of ResultInit object. Reason: ${e.message}",
            this::updateResultInitStatus.name
        )

        val resultInit = try {
            resultInitStorageService.findResultInitById(updateResultInitStatusCommand.id)
        } catch (e: Exception) {
            logError(e)
            null
        }

        return resultInit?.let { resultInitNotNull ->
            try {
                resultInitStorageService.updateResultInitStatus(
                    resultInitNotNull, updateResultInitStatusCommand.status
                )
            } catch (e: Exception) {
                logError(e)
                null
            }
        }
    }
}
