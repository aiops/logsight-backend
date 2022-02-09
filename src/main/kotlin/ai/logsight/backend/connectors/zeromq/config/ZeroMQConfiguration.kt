package ai.logsight.backend.connectors.zeromq.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.net.ConnectException

enum class ConnectionType {
    BIND, CONNECT
}

@Configuration
class ZeroMQConfiguration(
    private val zeroMqConfig: ZeroMQConfigurationProperties
) {
    @Bean
    @Qualifier("pub")
    fun zeroMQPubSocket(): ZMQ.Socket {
        return createSocket(SocketType.PUB, zeroMqConfig.pubPort, ConnectionType.BIND)
    }

    @Bean
    @Qualifier("req")
    fun zeroMQReqSocket(): ZMQ.Socket {
        return createSocket(SocketType.REQ, zeroMqConfig.reqPort, ConnectionType.CONNECT)
    }

    private fun createSocket(socketType: SocketType, port: Int, connectionType: ConnectionType): ZMQ.Socket {
        val ctx = ZContext()
        val zeroMQSocket = ctx.createSocket(socketType)
        if (socketType == SocketType.REQ) {
            zeroMQSocket.receiveTimeOut = zeroMqConfig.reqTimeout // seconds
        }
        val addr = "${zeroMqConfig.protocol}://${zeroMqConfig.host}:$port"

        if (connectionType == ConnectionType.CONNECT) {
            val status = zeroMQSocket.connect(addr)
            if (!status) throw ConnectException("ZeroMQ is not able to connect socket to $addr")
        } else {
            val status = zeroMQSocket.bind(addr)
            if (!status) throw ConnectException("ZeroMQ is not able to bind socket to $addr")
        }
        return zeroMQSocket
    }
}
