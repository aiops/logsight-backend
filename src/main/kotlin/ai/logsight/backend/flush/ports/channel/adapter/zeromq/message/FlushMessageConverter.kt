package ai.logsight.backend.flush.ports.channel.adapter.zeromq.message

import ai.logsight.backend.common.utils.TopicJsonSerializer
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.converter.MessageConverter
import org.springframework.messaging.support.ErrorMessage
import org.springframework.messaging.support.GenericMessage
import org.springframework.stereotype.Service

@Service
class FlushMessageConverter(
    private val topicJsonString: TopicJsonSerializer
) : MessageConverter {

    override fun fromMessage(message: Message<*>, targetClass: Class<*>): Message<*> {
        throw NotImplementedError()
    }

    override fun toMessage(payload: Any, headers: MessageHeaders?): Message<*>? {
        return if (payload is ByteArray) {
            try {
                GenericMessage(topicJsonString.deserialize(String(payload), FlushMessage::class.java))
            } catch (e: Exception) {
                ErrorMessage(
                    RuntimeException(
                        "Error during deserialization of FlushMessage payload $payload. " +
                            "Reason: ${e.message}"
                    )
                )
            }
        } else {
            null
        }
    }
}
