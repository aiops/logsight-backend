package ai.logsight.backend.results.ports.rpc.adapters.zeromq.config

import ai.logsight.backend.results.domain.service.ResultService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.FluxMessageChannel
import org.springframework.integration.support.json.EmbeddedJsonHeadersMessageMapper
import org.springframework.integration.zeromq.inbound.ZeroMqMessageProducer
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.converter.GenericMessageConverter
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.net.ConnectException


@Configuration
class ResultInitRPCZeroMqConfig(
    private val resultInitRPCZeroMqConfigProperties: ResultInitRPCZeroMqConfigProperties
) {
    @Bean
    fun resultInitRPCSocketPub(): ZMQ.Socket {
        val zmqContext = ZContext()
        val endpoint = "${resultInitRPCZeroMqConfigProperties.protocol}://" +
            "${resultInitRPCZeroMqConfigProperties.host}:" +
            "${resultInitRPCZeroMqConfigProperties.port}"
        val socket = zmqContext.createSocket(SocketType.PUB)
        val status = try {
            socket.bind(endpoint)
        } catch (e: Exception) {
            throw ConnectException("ZeroMQ socket is not able to connect socket to $endpoint. Reason: ${e.message}")
        }
        if (!status) throw ConnectException("ZeroMQ is not able to connect socket to $endpoint.")
        return socket
    }
}
