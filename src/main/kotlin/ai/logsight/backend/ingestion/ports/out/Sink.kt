package ai.logsight.backend.ingestion.ports.out

import ai.logsight.backend.connectors.Connector

class Sink(
    val connector: Connector
) {
//    fun sendData(fileContent: List<com.loxbear.logsight.models.log.LogMessage>) {
//        fileContent.forEach { logMessage -> connector.sendData(logMessage.message) }
//    }
}
