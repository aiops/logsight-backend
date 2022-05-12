package ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.zeromq.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.net.ConnectException

@Configuration
class LogSinkZeroMqConfig(
    private val logSinkZeroMqConfig: LogSinkZeroMqConfigProperties
) {
    @Bean
    fun logStreamZeroMqSocket(): ZMQ.Socket {
        val ctx = ZContext()
        val zeroMQSocket = ctx.createSocket(SocketType.PUB)
        zeroMQSocket.sndHWM = logSinkZeroMqConfig.hwm
        zeroMQSocket.setXpubNoDrop(true)
        val addr = "${logSinkZeroMqConfig.protocol}://${logSinkZeroMqConfig.host}:${logSinkZeroMqConfig.port}"
        val status = zeroMQSocket.bind(addr)
        if (!status) throw ConnectException("ZeroMQ log stream is not able to bind socket to $addr")
        return zeroMQSocket
    }
}
