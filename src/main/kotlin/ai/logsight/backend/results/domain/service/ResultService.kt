package ai.logsight.backend.results.domain.service

import ai.logsight.backend.results.domain.ResultInit
import ai.logsight.backend.results.domain.service.command.CreateResultInitCommand

interface ResultService {
    fun createResultInit(createResultInitCommand: CreateResultInitCommand): ResultInit
}