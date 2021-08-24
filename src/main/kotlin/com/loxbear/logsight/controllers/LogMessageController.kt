package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.enums.LogFileTypes
import com.loxbear.logsight.models.log.LogMessageLogsight
import com.loxbear.logsight.repositories.kafka.LogRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/applications/{appID}/logMessage")
class LogMessageController(
    val logRepository: LogRepository
) {


    @PutMapping("/")
    fun putLogMessage(
        authentication: Authentication,
        @PathVariable appID: Long,
        @RequestParam logMessage: LogMessageLogsight
    ): ResponseEntity<LogMessageLogsight> {
        logRepository.toKafka(authentication.name, appID, LogFileTypes.LOGSIGHT_JSON, listOf(logMessage))

        return ResponseEntity<LogMessageLogsight>(
            logMessage,
            HttpStatus.OK
        )
    }

}