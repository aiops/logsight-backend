package ai.logsight.backend.connectors.sink.zmq.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.net.ConnectException

@Configuration
class ZmqConfig(
    private val zmqConfigProperties: ZmqConfigProperties
) {
    @Bean
    fun zmqSocket(): ZMQ.Socket {
        val ctx = ZContext()
        val zeroMQSocket = ctx.createSocket(SocketType.PUB)
        zeroMQSocket.sndHWM = zmqConfigProperties.hwm
        zeroMQSocket.setXpubNoDrop(true)
        val addr = "${zmqConfigProperties.protocol}://${zmqConfigProperties.host}:${zmqConfigProperties.port}"
        val status = zeroMQSocket.bind(addr)
        if (!status) throw ConnectException("ZeroMQ log stream is not able to bind socket to $addr")
        return zeroMQSocket
    }
}
