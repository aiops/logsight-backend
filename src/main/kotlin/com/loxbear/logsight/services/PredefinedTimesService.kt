package com.loxbear.logsight.services

import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.entities.PredefinedTime
import com.loxbear.logsight.models.PredefinedTimeRequest
import com.loxbear.logsight.repositories.PredefinedTimesRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PredefinedTimesService(
    val repository: PredefinedTimesRepository,
) {
    val logger = LoggerFactory.getLogger(PredefinedTimesService::class.java)

    fun findAllByUser(user: LogsightUser): List<PredefinedTime> = repository.findAllByUser(user)

    fun createPredefinedTimesForUser(user: LogsightUser, request: PredefinedTimeRequest): PredefinedTime {
        return with(request) {
            val predefinedTime = PredefinedTime(
                id = 0, name = name, startTime = startTime, endTime = endTime, dateTimeType = dateTimeType, user = user
            )
            logger.info("Saving predefined time [{}] for user with key [{}]", predefinedTime, user.key)
            repository.save(predefinedTime)
        }
    }

    fun deleteById(id: Long) {
        logger.info("deleting predefined time with id [{}]", id)
        repository.deleteById(id)
    }
}