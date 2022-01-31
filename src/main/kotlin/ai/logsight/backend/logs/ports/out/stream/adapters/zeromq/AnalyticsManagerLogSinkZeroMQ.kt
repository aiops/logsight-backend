package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq

import ai.logsight.backend.logs.ports.out.stream.AnalyticsManagerLogSink
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.zeromq.ZMQ

@Component
class AnalyticsManagerLogSinkZeroMQ : AnalyticsManagerLogSink {
    @Autowired
    lateinit var zmqSocket: ZMQ.Socket

    override fun sendLogs(msg: String): Int {
        zmqSocket.send(msg)
        println("Sent $msg")
        return 0
    }
}
