package ai.logsight.backend.results.ports.rpc.adapters.zeromq.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "logsight.result-init.rpc.zeromq")
@ConstructorBinding
class ResultInitRPCZeroMqConfigProperties(
    val protocol: String = "tcp",
    val host: String = "0.0.0.0",
    val port: Int = 5557
)
