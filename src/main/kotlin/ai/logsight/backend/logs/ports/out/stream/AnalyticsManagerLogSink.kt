package ai.logsight.backend.logs.ports.out.stream

import ai.logsight.backend.logs.ports.out.stream.dto.LogDTO

interface AnalyticsManagerLogSink {
    fun sendLogs(msg: String): Int
}