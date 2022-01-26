package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq

import ai.logsight.backend.logs.ports.out.stream.dto.LogDTO
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class AnalyticsManagerLogSinkZeroMQTest {

    @Test
    fun sendLogs() {
        val zeromq = AnalyticsManagerLogSinkZeroMQ()
        for (i in 1..3) {
            zeromq.sendLogs("$i")
        }

    }
}
