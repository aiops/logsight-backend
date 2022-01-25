package ai.logsight.backend.common.config.connectors.elasticsearch

import ai.logsight.backend.common.config.connectors.Connector
import javax.persistence.Entity

class ESConnector(
    override var id: Long,
    val indexName: String,

) : Connector(id)
