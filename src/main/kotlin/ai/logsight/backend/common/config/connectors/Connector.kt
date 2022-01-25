package ai.logsight.backend.common.config.connectors

import javax.persistence.Entity

abstract class Connector(
    open var id: Long = 0
) {
//    abstract fun toEntity(): ConnectorEntity
}
