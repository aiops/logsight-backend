package ai.logsight.backend.application.ports.out.rpc.adapters.zeromq.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.net.ConnectException

@Configuration
class RPCConfigZeroMq(
    private val RPCConfigPropertiesZeroMq: RPCConfigPropertiesZeroMq
) {
    @Bean
    fun zeroMqRPCSocket(): ZMQ.Socket {
        val ctx = ZContext()
        val socket = ctx.createSocket(SocketType.REQ)
        val addr = "${RPCConfigPropertiesZeroMq.protocol}://${RPCConfigPropertiesZeroMq.host}:${RPCConfigPropertiesZeroMq.port}"
        socket.reqRelaxed = true
        socket.receiveTimeOut = RPCConfigPropertiesZeroMq.timeout
        val status = socket.connect(addr)
        if (!status) throw ConnectException("ZeroMQ is not able to connect socket to $addr")
        return socket
    }
}
