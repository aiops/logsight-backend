package ai.logsight.backend.timeselection.domain.service

import ai.logsight.backend.timeselection.domain.TimeSelection
import ai.logsight.backend.timeselection.extensions.toTimeSelectionEntity
import ai.logsight.backend.timeselection.ports.out.persistence.DateTimeType
import ai.logsight.backend.timeselection.ports.out.persistence.TimeSelectionEntity
import ai.logsight.backend.timeselection.ports.web.request.PredefinedTimeRequest
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.extensions.toUserEntity
import ai.logsight.backend.users.ports.out.persistence.TimeSelectionStorageService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TimeSelectionServiceImpl(
    private val timeSelectionStorageService: TimeSelectionStorageService,
) : TimeSelectionService {

    val logger: Logger = LoggerFactory.getLogger(TimeSelectionService::class.java)

    override fun findAllByUser(user: User): List<TimeSelection> = timeSelectionStorageService.findAllByUser(user)

    override fun createTimeSelection(user: User, request: PredefinedTimeRequest): TimeSelection {
        val timeSelection = TimeSelection(
            id = 0,
            name = request.name,
            startTime = request.startTime,
            endTime = request.endTime,
            dateTimeType = request.dateTimeType,
            user = user.toUserEntity()
        )
        logger.info("Saving predefined time [{}] for user with key [{}]", timeSelection, user.key)
        return timeSelectionStorageService.saveTimeSelection(timeSelection.toTimeSelectionEntity())
    }

    override fun deleteTimeSelection(id: Long) {
        logger.info("deleting predefined time with id [{}]", id)
        timeSelectionStorageService.deleteTimeSelectionById(id)
    }

    override fun createPredefinedTimeSelections(user: User): List<TimeSelection> {
        logger.info("saving default predefined times for user with id [{}]", user.id)
        val timeSelectionSelections = listOf(
            TimeSelectionEntity(
                id = 0,
                startTime = "now-60m",
                endTime = "now",
                name = "1H",
                dateTimeType = DateTimeType.RELATIVE,
                user = user.toUserEntity()
            ),
            TimeSelectionEntity(
                id = 0,
                startTime = "now-180m",
                endTime = "now",
                name = "3H",
                dateTimeType = DateTimeType.RELATIVE,
                user = user.toUserEntity()
            ),
            TimeSelectionEntity(
                id = 0,
                startTime = "now-720m",
                endTime = "now",
                name = "12H",
                dateTimeType = DateTimeType.RELATIVE,
                user = user.toUserEntity()
            ),
            TimeSelectionEntity(
                id = 0,
                startTime = "now-1440m",
                endTime = "now",
                name = "1D",
                dateTimeType = DateTimeType.RELATIVE,
                user = user.toUserEntity()
            ),
            TimeSelectionEntity(
                id = 0,
                startTime = "now-4320m",
                endTime = "now",
                name = "3D",
                dateTimeType = DateTimeType.RELATIVE,
                user = user.toUserEntity()
            ),
            TimeSelectionEntity(
                id = 0,
                startTime = "now-10080m",
                endTime = "now",
                name = "1W",
                dateTimeType = DateTimeType.RELATIVE,
                user = user.toUserEntity()
            ),
            TimeSelectionEntity(
                id = 0,
                startTime = "now-20160m",
                endTime = "now",
                name = "2W",
                dateTimeType = DateTimeType.RELATIVE,
                user = user.toUserEntity()
            ),
            TimeSelectionEntity(
                id = 0,
                startTime = "now-43800m",
                endTime = "now",
                name = "1M",
                dateTimeType = DateTimeType.RELATIVE,
                user = user.toUserEntity()
            ),
            TimeSelectionEntity(
                id = 0,
                startTime = "now-525600m",
                endTime = "now",
                name = "1Y",
                dateTimeType = DateTimeType.RELATIVE,
                user = user.toUserEntity()
            )
        )
        return timeSelectionStorageService.saveAllTimeSelections(timeSelectionSelections)
    }
}
