package ai.logsight.backend.common.utils

import org.springframework.stereotype.Service

@Service
class TopicBuilder {
    fun buildTopic(elements: Collection<String>): String = elements.joinToString(separator = "_")
}
