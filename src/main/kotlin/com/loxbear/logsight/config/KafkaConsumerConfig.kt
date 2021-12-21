package com.loxbear.logsight.config

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory

import org.springframework.kafka.core.DefaultKafkaConsumerFactory

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import java.util.HashMap

import org.springframework.kafka.core.ConsumerFactory

import org.springframework.kafka.annotation.EnableKafka


@EnableKafka
@Configuration
class KafkaConsumerConfig(
    @Autowired val kafkaProperties: KafkaProperties
) {

    fun consumerFactory(): ConsumerFactory<String, String> {
        val props: MutableMap<String, Any> = HashMap(kafkaProperties.buildConsumerProperties())
        println(props)
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory()
        return factory
    }
}