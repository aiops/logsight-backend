package ai.logsight.backend.flush.ports.channel.adapter.zeromq.config

import ai.logsight.backend.flush.ports.channel.adapter.zeromq.FlushMessageHandlerZeroMq
import ai.logsight.backend.flush.ports.channel.adapter.zeromq.message.FlushMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.FluxMessageChannel
import org.springframework.integration.zeromq.inbound.ZeroMqMessageProducer
import org.zeromq.SocketType
import org.zeromq.ZContext

@Configuration
class FlushMessageHandlerZeroMqConfig(
    private val flushMessageHandlerZeroMqConfigProperties: FlushMessageHandlerZeroMqConfigProperties,
    private val flushMessageHandlerZeroMq: FlushMessageHandlerZeroMq
) {
    @Bean
    fun zeroMqMessageChannel(): FluxMessageChannel = FluxMessageChannel()

    @Bean
    fun zeroMqPubProducer(
        zeroMqMessageChannel: FluxMessageChannel,
        converter: FlushMessageConverter
    ): ZeroMqMessageProducer {
        val zmqContext = ZContext()
        val zeroMqMessageProducer = ZeroMqMessageProducer(zmqContext, SocketType.SUB)
        zeroMqMessageProducer.setTopics(flushMessageHandlerZeroMqConfigProperties.topic)
        zeroMqMessageProducer.setBindPort(flushMessageHandlerZeroMqConfigProperties.port)
        zeroMqMessageProducer.outputChannel = zeroMqMessageChannel
        zeroMqMessageProducer.setMessageConverter(converter)
        return zeroMqMessageProducer
    }

    @Bean
    @ServiceActivator(inputChannel = "zeroMqMessageChannel")
    fun subscribe(): FlushMessageHandlerZeroMq = flushMessageHandlerZeroMq
}
