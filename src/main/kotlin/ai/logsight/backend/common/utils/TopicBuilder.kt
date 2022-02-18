package ai.logsight.backend.common.utils

class TopicBuilder {
    fun buildTopic(elements: Collection<String>): String = elements.joinToString(separator = "_")
}
