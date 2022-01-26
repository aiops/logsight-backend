package ai.logsight.backend.connectors

interface Connector {
    fun sendData(message: String)
}
