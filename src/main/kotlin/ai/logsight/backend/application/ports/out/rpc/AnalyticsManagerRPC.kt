package ai.logsight.backend.application.ports.out.rpc

import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTO

interface AnalyticsManagerRPC {
    fun createApplication(createApplicationDTO: ApplicationDTO)
    fun deleteApplication(deleteApplicationDTO: ApplicationDTO)
}
