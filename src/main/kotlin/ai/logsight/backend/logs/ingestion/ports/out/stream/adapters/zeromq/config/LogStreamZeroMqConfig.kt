package ai.logsight.backend.logs.ingestion.ports.out.stream.adapters.zeromq.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.net.ConnectException

@Configuration
class LogStreamZeroMqConfig(
    private val logStreamZeroMqConfig: LogStreamZeroMqConfigProperties
) {
    @Bean
    fun logStreamZeroMqSocket(): ZMQ.Socket {
        val ctx = ZContext()
        val zeroMQSocket = ctx.createSocket(SocketType.PUB)
        zeroMQSocket.sndHWM = 10000000
        zeroMQSocket.setXpubNoDrop(true)
        val addr = "${logStreamZeroMqConfig.protocol}://${logStreamZeroMqConfig.host}:${logStreamZeroMqConfig.port}"
        val status = zeroMQSocket.bind(addr)
        if (!status) throw ConnectException("ZeroMQ log stream is not able to bind socket to $addr")
        return zeroMQSocket
    }
}
