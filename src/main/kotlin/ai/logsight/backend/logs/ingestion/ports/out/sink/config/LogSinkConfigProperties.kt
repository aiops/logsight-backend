package ai.logsight.backend.logs.ingestion.ports.out.sink.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConfigurationProperties(prefix = "logsight.logs.sink")
@ConstructorBinding
@EnableConfigurationProperties
class LogSinkConfigProperties(
    val type: LogSinkTypes
)
