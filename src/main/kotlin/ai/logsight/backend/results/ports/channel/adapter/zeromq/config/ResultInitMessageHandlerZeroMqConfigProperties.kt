package ai.logsight.backend.results.ports.channel.adapter.zeromq.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "logsight.result-init.message-handler.zeromq")
@ConstructorBinding
class ResultInitMessageHandlerZeroMqConfigProperties(
    val protocol: String = "tcp",
    val host: String = "0.0.0.0",
    val port: Int = 5558,
    val subTopic: String = "result_init"
)
