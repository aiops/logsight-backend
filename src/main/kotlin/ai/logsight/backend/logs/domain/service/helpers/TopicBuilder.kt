package ai.logsight.backend.logs.domain.service.helpers

import org.springframework.stereotype.Service

@Service
class TopicBuilder {
    fun buildTopic(userKey: String, appName: String): String = "${userKey}_${appName}_input"
}
