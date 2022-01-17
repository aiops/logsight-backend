package ai.logsight.backend.application.ports.out.communication

import ai.logsight.backend.application.ports.out.communication.dto.ApplicationDTO

interface LogsightAnalyticsManager {
    fun createApplication(createApplicationDTO: ApplicationDTO)
    fun deleteApplication(deleteApplicationDTO: ApplicationDTO)
}
