package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq

import org.springframework.integration.zeromq.inbound.ZeroMqMessageProducer
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.zeromq.SocketType
import org.zeromq.ZContext
import zmq.Msg
import java.time.Duration

fun main() {
    val uri = "tcp://127.0.0.1:3006"
    val context = ZContext(1)
    val socket = context.createSocket(SocketType.PUSH)

    socket.hwm = 10
    socket.linger = 1
    println("connecting to $uri")
    socket.connect(uri)

    fun publish(path: String, msg: String) {
        socket.sendMore(path)
        socket.send(msg.toByteArray())
    }

    var count = 0

    for (i in 1..5) {
        val msg = "message : ${++count}"
        socket.send("test message".toByteArray())
        publish("", msg)
        println(msg)
    }
}
