package ai.logsight.backend.results.ports.persistence

import ai.logsight.backend.results.domain.ResultInit
import ai.logsight.backend.results.domain.service.ResultInitStatus
import ai.logsight.backend.results.domain.service.command.CreateResultInitCommand
import java.util.*

interface ResultInitStorageService {
    fun saveResultInit(createResultInitCommand: CreateResultInitCommand): ResultInit
    fun deleteResultInit(resultInit: ResultInit)
    fun findAllResultInitByStatusAndApplicationId(status: ResultInitStatus, applicationId: UUID): List<ResultInit>
    fun updateResultInitStatus(resultInit: ResultInit, status: ResultInitStatus): ResultInit
}
