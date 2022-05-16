package ai.logsight.backend.logs.ingestion.ports.out.log_sink.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConfigurationProperties(prefix = "logsight.connector")
@ConstructorBinding
@EnableConfigurationProperties
class LogSinkConfigProperties(
    val type: LogSinkConnectorTypes
)
