package com.loxbear.logsight.services

import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.entities.TimeSelection
import com.loxbear.logsight.entities.enums.DateTimeType
import com.loxbear.logsight.models.PredefinedTimeRequest
import com.loxbear.logsight.repositories.PredefinedTimesRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TimeSelectionService(
    val repository: PredefinedTimesRepository,
) {
    val logger: Logger = LoggerFactory.getLogger(TimeSelectionService::class.java)

    fun findAllByUser(user: LogsightUser): List<TimeSelection> = repository.findAllByUser(user)

    fun createTimeSelection(user: LogsightUser, request: PredefinedTimeRequest): TimeSelection {
        return with(request) {
            val timeSelection = TimeSelection(
                id = 0, name = name, startTime = startTime, endTime = endTime, dateTimeType = dateTimeType, user = user
            )
            logger.info("Saving predefined time [{}] for user with key [{}]", timeSelection, user.key)
            repository.save(timeSelection)
        }
    }

    fun deleteTimeSelection(id: Long) {
        logger.info("deleting predefined time with id [{}]", id)
        repository.deleteById(id)
    }

    fun createPredefinedTimeSelections(user: LogsightUser): List<TimeSelection> {
        logger.info("saving default predefined times for user with id [{}]", user.id)
        val timeSelectionSelections = listOf(
            TimeSelection(id = 0, startTime = "now-60m", endTime = "now", name = "1H", dateTimeType = DateTimeType.RELATIVE, user = user),
            TimeSelection(id = 0, startTime = "now-180m", endTime = "now", name = "3H", dateTimeType = DateTimeType.RELATIVE, user = user),
            TimeSelection(id = 0, startTime = "now-720m", endTime = "now", name = "12H", dateTimeType = DateTimeType.RELATIVE,user = user),
            TimeSelection(id = 0, startTime = "now-1440m", endTime = "now", name = "1D", dateTimeType = DateTimeType.RELATIVE,user = user),
            TimeSelection(id = 0, startTime = "now-4320m", endTime = "now", name = "3D", dateTimeType = DateTimeType.RELATIVE,user = user),
            TimeSelection(id = 0, startTime = "now-10080m", endTime = "now", name = "1W", dateTimeType = DateTimeType.RELATIVE,user = user),
            TimeSelection(id = 0, startTime = "now-20160m", endTime = "now", name = "2W", dateTimeType = DateTimeType.RELATIVE,user = user),
            TimeSelection(id = 0, startTime = "now-43800m", endTime = "now", name = "1M", dateTimeType = DateTimeType.RELATIVE,user = user),
            TimeSelection(id = 0, startTime = "now-525600m", endTime = "now", name = "1Y", dateTimeType = DateTimeType.RELATIVE,user = user)
        )
        repository.saveAll(timeSelectionSelections)
        return timeSelectionSelections
    }
}
