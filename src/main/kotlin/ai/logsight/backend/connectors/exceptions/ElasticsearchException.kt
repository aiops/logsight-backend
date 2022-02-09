package ai.logsight.backend.connectors.exceptions

class ElasticsearchException(override val message: String? = null) : RuntimeException(message)
