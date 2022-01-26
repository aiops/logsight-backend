package ai.logsight.backend.connectors.sink

import ai.logsight.backend.connectors.Connector

interface Sink : Connector {
    fun sendData(data: String)
}
