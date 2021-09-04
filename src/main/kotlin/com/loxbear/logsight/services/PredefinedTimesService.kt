package com.loxbear.logsight.services

import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.entities.PredefinedTime
import com.loxbear.logsight.entities.enums.DateTimeType
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

    fun createDefaultPredefinedTimesForUser(user: LogsightUser) {
        logger.info("saving default predefined times for user with id [{}]", user.id)
        repository.saveAll(listOf(
            PredefinedTime(id = 0, startTime = "now-60m", endTime = "now", name = "1H", dateTimeType = DateTimeType.RELATIVE, user = user),
            PredefinedTime(id = 0, startTime = "now-180m", endTime = "now", name = "3H", dateTimeType = DateTimeType.RELATIVE, user = user),
            PredefinedTime(id = 0, startTime = "now-720m", endTime = "now", name = "12H", dateTimeType = DateTimeType.RELATIVE,user = user),
            PredefinedTime(id = 0, startTime = "now-1440m", endTime = "now", name = "1D", dateTimeType = DateTimeType.RELATIVE,user = user),
            PredefinedTime(id = 0, startTime = "now-4320m", endTime = "now", name = "3D", dateTimeType = DateTimeType.RELATIVE,user = user),
            PredefinedTime(id = 0, startTime = "now-10080m", endTime = "now", name = "1W", dateTimeType = DateTimeType.RELATIVE,user = user),
            PredefinedTime(id = 0, startTime = "now-20160m", endTime = "now", name = "2W", dateTimeType = DateTimeType.RELATIVE,user = user))
        )
    }
}