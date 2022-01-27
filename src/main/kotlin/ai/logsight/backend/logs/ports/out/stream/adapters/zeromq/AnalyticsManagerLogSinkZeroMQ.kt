package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq

import ai.logsight.backend.logs.ports.out.stream.AnalyticsManagerLogSink
import ai.logsight.backend.logs.ports.out.stream.adapters.zeromq.config.AnalyticsManagerLogSinkConfiguration
import ai.logsight.backend.logs.ports.out.stream.dto.LogDTO
import org.springframework.stereotype.Component
import org.zeromq.ZMQ

@Component
class AnalyticsManagerLogSinkZeroMQ : AnalyticsManagerLogSink {

    val zmqSocket: ZMQ.Socket = AnalyticsManagerLogSinkConfiguration().zeroMqPubSocket()
    override fun sendLogs(msg: String): Int {
        zmqSocket.send(msg)
        println("Sent ${msg}")
        return 0
    }
}
