package ai.logsight.backend.application.service

import ai.logsight.backend.application.domain.Application

interface ApplicationManagementService {
    fun createApplication(): Application
    fun deleteApplication(): Application
}
