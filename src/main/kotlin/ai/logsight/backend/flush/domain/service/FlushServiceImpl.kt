package ai.logsight.backend.flush.domain.service

import ai.logsight.backend.common.logging.Logger
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.common.utils.TopicBuilder
import ai.logsight.backend.flush.domain.Flush
import ai.logsight.backend.flush.domain.service.command.CreateFlushCommand
import ai.logsight.backend.flush.domain.service.command.UpdateFlushStatusCommand
import ai.logsight.backend.flush.domain.service.query.FindFlushQuery
import ai.logsight.backend.flush.ports.persistence.FlushStorageService
import ai.logsight.backend.flush.ports.rpc.FlushRPCService
import ai.logsight.backend.flush.ports.rpc.dto.FlushDTO
import ai.logsight.backend.flush.ports.rpc.dto.FlushDTOOperations
import com.antkorwin.xsync.XSync
import org.springframework.stereotype.Service

@Service
class FlushServiceImpl(
    val flushStorageService: FlushStorageService,
    val flushRPCService: FlushRPCService,
    val xSync: XSync<String>
) : FlushService {
    val topicBuilder = TopicBuilder()
    private val logger: Logger = LoggerImpl(FlushServiceImpl::class.java)

    // TODO make configurable
    private var topicPostfix: String = "input_ctrl"

    override fun createFlush(createFlushCommand: CreateFlushCommand): Flush {
        // This is needed due to the mutex block. Later it's ugly with ?.let There might be a better way?
        // try to load Flush where status is in PENDING
        val flush = flushStorageService.saveFlush(createFlushCommand)

        return flush.let { flushNotNull ->
            // Create DTO to transfer to logsight core
            val flushDTO = FlushDTO(
                id = flushNotNull.id,
                orderNum = flushNotNull.logsReceipt.orderNum,
                logsCount = flushNotNull.logsReceipt.logsCount,
                operation = FlushDTOOperations.FLUSH
            )
            // create topic
            val topic = topicBuilder.buildTopic(
                listOf(createFlushCommand.user.key, createFlushCommand.application.name, topicPostfix)
            )
            // Send flush command to logsight core
            try {
                flushRPCService.flush(topic, flushDTO)
            } catch (e: Exception) {
                // Revert DB entry if sending failed
                logger.error(
                    "Failed to send flush RPC $flushDTO. Reason: ${e.message}", this::updateFlushStatus.name
                )
                flushStorageService.deleteFlush(flushNotNull)
                throw RuntimeException("Failed to send flush RPC $flushDTO. Reason: ${e.message}")
            }
            flushNotNull
        }
    }

    override fun findFlush(findFlushQuery: FindFlushQuery): Flush {
        return flushStorageService.findFlushById(findFlushQuery.flushId)
    }

    override fun updateFlushStatus(updateFlushStatusCommand: UpdateFlushStatusCommand): Flush? {
        fun logError(e: Exception) = logger.error(
            "Failed to update status of Flush object. Reason: ${e.message}", this::updateFlushStatus.name
        )

        val flush = try {
            flushStorageService.findFlushById(updateFlushStatusCommand.id)
        } catch (e: Exception) {
            logError(e)
            null
        }

        return flush?.let { flushNotNull ->
            try {
                flushStorageService.updateFlushStatus(
                    flushNotNull, updateFlushStatusCommand.status
                )
            } catch (e: Exception) {
                logError(e)
                null
            }
        }
    }
}
