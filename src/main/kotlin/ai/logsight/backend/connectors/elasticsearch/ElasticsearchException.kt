package ai.logsight.backend.connectors.elasticsearch

class ElasticsearchException(override val message: String? = null) : RuntimeException(message)
