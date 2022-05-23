package ai.logsight.backend.connectors.sink

interface SinkConnector {
    fun send(msg: String): Boolean
}
