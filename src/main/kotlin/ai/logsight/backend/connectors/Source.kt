package ai.logsight.backend.connectors

interface Source : Connector {
    fun getData()
}
