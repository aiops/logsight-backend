package ai.logsight.backend.connectors.log_sink

interface LogSinkConnector {
    fun send(msg: String): Boolean
}
