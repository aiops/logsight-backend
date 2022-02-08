package ai.logsight.backend.exceptions

class ElasticsearchException(override val message: String? = null) : LogsightApplicationException(message)
