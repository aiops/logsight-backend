package ai.logsight.backend.results.domain.service

import ai.logsight.backend.results.domain.ResultInit
import ai.logsight.backend.results.domain.service.command.CreateResultInitCommand
import ai.logsight.backend.results.domain.service.command.UpdateResultInitStatusCommand

interface ResultService {
    fun createResultInit(createResultInitCommand: CreateResultInitCommand): ResultInit
    fun updateResultInitStatus(updateResultInitStatusCommand: UpdateResultInitStatusCommand): ResultInit?
}
