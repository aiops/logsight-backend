package ai.logsight.backend.application.ports.out.rpc

import ai.logsight.backend.application.ports.out.rpc.adapters.repsponse.RPCResponse
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTO

interface AnalyticsManagerRPC {
    fun createApplication(createApplicationDTO: ApplicationDTO): RPCResponse?
    fun deleteApplication(deleteApplicationDTO: ApplicationDTO): RPCResponse?
}
