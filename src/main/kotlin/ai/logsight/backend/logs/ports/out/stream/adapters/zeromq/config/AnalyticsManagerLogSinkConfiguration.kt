package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zeromq.SocketType
import org.zeromq.ZMQ

@Configuration
class AnalyticsManagerLogSinkConfiguration(
    val logSinkConfig: AnalyticsManagerLogSinkConfigurationProperties,
) {

    @Bean
    fun zeroMqPubSocket(): ZMQ.Socket {
        val context = ZMQ.context(1)
        val socket = context.socket(SocketType.PULL)
        socket.bind("${logSinkConfig.protocol}://${logSinkConfig.host}:${logSinkConfig.port}")
        return socket
    }
}
