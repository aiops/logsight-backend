package ai.logsight.backend.connectors.elasticsearch

import ai.logsight.backend.connectors.Connector
import javax.persistence.Entity

class ESConnector(
    override var id: Long,
    val indexName: String,

) : Connector(id)
