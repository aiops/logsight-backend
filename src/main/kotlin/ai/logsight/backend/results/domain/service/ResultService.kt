package ai.logsight.backend.results.domain.service

import ai.logsight.backend.results.domain.ResultInit
import ai.logsight.backend.results.domain.service.command.CreateResultInitCommand
import ai.logsight.backend.results.domain.service.command.UpdateResultInitStatusCommand
import java.util.*

interface ResultService {
    fun createResultInit(createResultInitCommand: CreateResultInitCommand): ResultInit
    fun getResultInit(resultInitId: UUID): ResultInit
    fun updateResultInitStatus(updateResultInitStatusCommand: UpdateResultInitStatusCommand): ResultInit?
}
