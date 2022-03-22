package ai.logsight.backend.flush.ports.channel.adapter.zeromq

import ai.logsight.backend.common.logging.Logger
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.flush.domain.service.FlushService
import ai.logsight.backend.flush.domain.service.FlushStatus
import ai.logsight.backend.flush.domain.service.command.UpdateFlushStatusCommand
import ai.logsight.backend.flush.ports.channel.FlushMessageHandler
import ai.logsight.backend.flush.ports.channel.adapter.zeromq.message.FlushMessage
import org.springframework.messaging.Message
import org.springframework.messaging.support.ErrorMessage
import org.springframework.messaging.support.GenericMessage
import org.springframework.stereotype.Controller

@Controller
class FlushMessageHandlerZeroMq(
    val flushService: FlushService,
) : FlushMessageHandler {

    private val logger: Logger = LoggerImpl(FlushMessageHandlerZeroMq::class.java)

    override fun handleMessage(message: Message<*>) {
        when (message) {
            is ErrorMessage -> logger.error("${message.payload.message}", this::handleMessage.name)
            is GenericMessage -> {
                if (message.payload is FlushMessage) {
                    val flushMessage = message.payload as FlushMessage
                    logger.info("Received Flush message: $flushMessage")
                    val updateFlushStatusCommand = UpdateFlushStatusCommand(
                        id = flushMessage.id,
                        status = FlushStatus.toFlushStatus(flushMessage.status)
                    )
                    flushService.updateFlushStatus(updateFlushStatusCommand)
                }
            }
        }
    }
}
