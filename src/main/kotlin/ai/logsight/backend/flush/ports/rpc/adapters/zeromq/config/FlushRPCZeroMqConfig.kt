package ai.logsight.backend.flush.ports.rpc.adapters.zeromq.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.net.ConnectException

@Configuration
class FlushRPCZeroMqConfig(
    private val flushRPCZeroMqConfigProperties: FlushRPCZeroMqConfigProperties
) {
    @Bean
    fun flushRPCSocketPub(): ZMQ.Socket {
        val zmqContext = ZContext()
        val endpoint = "${flushRPCZeroMqConfigProperties.protocol}://" +
            "${flushRPCZeroMqConfigProperties.host}:" +
            "${flushRPCZeroMqConfigProperties.port}"
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
