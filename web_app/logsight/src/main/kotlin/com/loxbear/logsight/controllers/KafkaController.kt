package com.loxbear.logsight.controllers

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.RestController

@RestController
class KafkaController(val kafkaTemplate: KafkaTemplate<String, String>) {

    fun send(){
        kafkaTemplate.send("asd", "")
    }
}