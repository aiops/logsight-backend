package com.loxbear.logsight.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaAdmin
import java.util.HashMap


@Configuration
class KafkaAdminConfig(
    @Autowired val kafkaProperties: KafkaProperties
) {

    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val props: MutableMap<String, Any> = HashMap(kafkaProperties.buildAdminProperties())
        println(props)
        return KafkaAdmin(props)
    }
}