package ai.logsight.backend.results.domain.service

import ai.logsight.backend.results.domain.ResultInit
import ai.logsight.backend.results.domain.service.command.CreateResultInitCommand
import ai.logsight.backend.results.domain.service.command.UpdateResultInitStatusCommand
import ai.logsight.backend.results.domain.service.query.FindResultInitQuery

interface ResultService {
    fun createResultInit(createResultInitCommand: CreateResultInitCommand): ResultInit
    fun findResultInit(findResultInitQuery: FindResultInitQuery): ResultInit
    fun updateResultInitStatus(updateResultInitStatusCommand: UpdateResultInitStatusCommand): ResultInit?
}
