package ai.logsight.backend.results.ports.channel.adapter.zeromq.config

import ai.logsight.backend.results.ports.channel.adapter.zeromq.ResultInitMessageHandlerZeroMq
import ai.logsight.backend.results.ports.channel.adapter.zeromq.message.ResultInitMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.FluxMessageChannel
import org.springframework.integration.zeromq.inbound.ZeroMqMessageProducer
import org.zeromq.SocketType
import org.zeromq.ZContext

@Configuration
class ResultInitMessageHandlerZeroMqConfig(
    private val resultInitMessageHandlerZeroMqConfigProperties: ResultInitMessageHandlerZeroMqConfigProperties,
    private val resultInitMessageHandlerZeroMq: ResultInitMessageHandlerZeroMq
) {
    @Bean
    fun zeroMqMessageChannel(): FluxMessageChannel = FluxMessageChannel()

    @Bean
    fun zeroMqPubProducer(
        zeroMqMessageChannel: FluxMessageChannel,
        converter: ResultInitMessageConverter
    ): ZeroMqMessageProducer {
        val zmqContext = ZContext()
        val zeroMqMessageProducer = ZeroMqMessageProducer(zmqContext, SocketType.SUB)
        zeroMqMessageProducer.setTopics(resultInitMessageHandlerZeroMqConfigProperties.topic)
        zeroMqMessageProducer.setBindPort(resultInitMessageHandlerZeroMqConfigProperties.port)
        zeroMqMessageProducer.outputChannel = zeroMqMessageChannel
        zeroMqMessageProducer.setMessageConverter(converter)
        return zeroMqMessageProducer
    }

    @Bean
    @ServiceActivator(inputChannel = "zeroMqMessageChannel")
    fun subscribe(): ResultInitMessageHandlerZeroMq = resultInitMessageHandlerZeroMq
}
