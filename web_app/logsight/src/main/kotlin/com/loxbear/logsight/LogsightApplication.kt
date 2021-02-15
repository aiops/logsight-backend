package com.loxbear.logsight

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LogsightApplication

fun main(args: Array<String>) {
	runApplication<LogsightApplication>(*args)
}
