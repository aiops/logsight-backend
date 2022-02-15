package ai.logsight.backend.results.ports.rpc.adapters.zeromq.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
        val endpoint = "${resultInitRPCZeroMqConfigProperties.protocol}://" +
            "${resultInitRPCZeroMqConfigProperties.host}:" +
            "${resultInitRPCZeroMqConfigProperties.pubPort}"
        return getSocket(endpoint, SocketType.PUB)
    }

    @Bean
    fun resultInitRPCSocketSub(): ZMQ.Socket {
        val endpoint = "${resultInitRPCZeroMqConfigProperties.protocol}://" +
            "${resultInitRPCZeroMqConfigProperties.host}:" +
            "${resultInitRPCZeroMqConfigProperties.subPort}"
        val socket = getSocket(endpoint, SocketType.SUB)
        socket.subscribe(resultInitRPCZeroMqConfigProperties.subTopic)
        return socket
    }

    private fun getSocket(endpoint: String, type: SocketType): ZMQ.Socket {
        val ctx = ZContext()
        val socket = ctx.createSocket(type)
        val status = try {
            socket.bind(endpoint)
        } catch (e: Exception) {
            throw ConnectException("ZeroMQ socket is not able to connect socket to $endpoint. Reason: ${e.message}")
        }
        if (!status) throw ConnectException("ZeroMQ is not able to connect socket to $endpoint.")
        return socket
    }
}
