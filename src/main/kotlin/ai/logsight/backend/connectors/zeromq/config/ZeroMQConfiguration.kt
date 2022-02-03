package ai.logsight.backend.connectors.zeromq.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.net.ConnectException

@Configuration
class ZeroMQConfiguration(
    private val logSinkConfig: ZeroMQConfigurationProperties
) {
    @Bean
    @Qualifier("pub")
    fun zeroMQPubSocket(): ZMQ.Socket {
        return createSocket(SocketType.PUB, logSinkConfig.pubPort)
    }

    @Bean
    @Qualifier("req")
    fun zeroMQReqSocket(): ZMQ.Socket {
        return createSocket(SocketType.REQ, logSinkConfig.reqPort)
    }

    private fun createSocket(socketType: SocketType, port: Int): ZMQ.Socket {
        val ctx = ZContext()
        val zeroMqPubSocket = ctx.createSocket(socketType)
        val adr = "${logSinkConfig.protocol}://${logSinkConfig.host}:$port"
        val rc = zeroMqPubSocket.bind(adr)
        if (!rc)
            throw ConnectException("ZeroMQ is not able to bind socket to $adr")
        return zeroMqPubSocket
    }
}
