package ai.logsight.backend.application.ports.out.rpc.adapters.zeromq

import ai.logsight.backend.application.ports.out.rpc.AnalyticsManagerRPC
import ai.logsight.backend.application.ports.out.rpc.adapters.repsponse.RPCResponse
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTO
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTOActions
import com.antkorwin.xsync.XSync
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.zeromq.ZMQ

@Service
@Qualifier("ZeroMQ")
class AnalyticsManagerZeroMQ(
    @Qualifier("req") val zeroMQReqSocket: ZMQ.Socket,
    val xSync: XSync<String>
) : AnalyticsManagerRPC {

    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    override fun createApplication(createApplicationDTO: ApplicationDTO): RPCResponse? {
        createApplicationDTO.action = ApplicationDTOActions.CREATE
        return sendZeroMqRPC(createApplicationDTO)
    }

    override fun deleteApplication(deleteApplicationDTO: ApplicationDTO): RPCResponse? {
        deleteApplicationDTO.action = ApplicationDTOActions.DELETE
        return sendZeroMqRPC(deleteApplicationDTO)
    }

    fun sendZeroMqRPC(applicationDTO: ApplicationDTO): RPCResponse? {
        var message: ByteArray? = null
        xSync.execute("logsight-rpc") { // TODO Move mutex definitions to somewhere else
            zeroMQReqSocket.send(mapper.writeValueAsString(applicationDTO))
            message = zeroMQReqSocket.recv()
        }
        return message?.let { mapper.readValue<RPCResponse>(it.decodeToString()) }
    }
}
