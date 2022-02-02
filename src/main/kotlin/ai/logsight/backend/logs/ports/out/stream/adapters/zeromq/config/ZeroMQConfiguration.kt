package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq.config

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
    fun zeroMqPubSocket(): ZMQ.Socket {
        val ctx = ZContext()
        val zeroMqPubSocket = ctx.createSocket(SocketType.PUB)
        val adr = "${logSinkConfig.protocol}://${logSinkConfig.host}:${logSinkConfig.port}"
        val rc = zeroMqPubSocket.bind(adr)
        if (!rc)
            throw ConnectException("ZeroMQ is not able to bind socket to $adr")
        return zeroMqPubSocket
    }

}
