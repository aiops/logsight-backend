package ai.logsight.backend.results.ports.channel.adapter.zeromq

import ai.logsight.backend.common.logging.Logger
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.results.domain.service.ResultInitStatus
import ai.logsight.backend.results.domain.service.ResultService
import ai.logsight.backend.results.domain.service.command.UpdateResultInitStatusCommand
import ai.logsight.backend.results.ports.channel.ResultInitMessageHandler
import ai.logsight.backend.results.ports.channel.adapter.zeromq.message.ResultInitMessage
import ai.logsight.backend.results.ports.persistence.ResultInitStorageService
import org.springframework.messaging.Message
import org.springframework.messaging.support.ErrorMessage
import org.springframework.messaging.support.GenericMessage
import org.springframework.stereotype.Controller

@Controller
class ResultInitMessageHandlerZeroMq(
    val resultService: ResultService,
) : ResultInitMessageHandler {

    private val logger: Logger = LoggerImpl(ResultInitMessageHandlerZeroMq::class.java)

    override fun handleMessage(message: Message<*>) {
        when (message) {
            is ErrorMessage -> logger.error(
                "Received ResultInit error message: ${message.payload.message}",
                this::handleMessage.name
            )
            is GenericMessage -> {
                if (message.payload is ResultInitMessage) {
                    val resultInitMessage = message.payload as ResultInitMessage
                    val updateResultInitStatusCommand = UpdateResultInitStatusCommand(
                        id = resultInitMessage.id,
                        status = ResultInitStatus.toResultInitStatus(resultInitMessage.status)
                    )
                    resultService.updateResultInitStatus(updateResultInitStatusCommand)
                }
            }
        }
    }
}