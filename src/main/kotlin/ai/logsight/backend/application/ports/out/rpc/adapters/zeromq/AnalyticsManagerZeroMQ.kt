package ai.logsight.backend.application.ports.out.rpc.adapters.zeromq

import ai.logsight.backend.application.ports.out.rpc.AnalyticsManagerRPC
import ai.logsight.backend.application.ports.out.rpc.adapters.repsponse.RPCResponse
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTO
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
        createApplicationDTO.action = "CREATE"
        return transmitRPC(createApplicationDTO)
    }

    override fun deleteApplication(deleteApplicationDTO: ApplicationDTO): RPCResponse? {
        deleteApplicationDTO.action = "DELETE"
        return transmitRPC(deleteApplicationDTO)
    }

    fun transmitRPC(applicationDTO: ApplicationDTO): RPCResponse? {
        var message: ByteArray? = null
        xSync.execute("analytics-rpc") {
            zeroMQReqSocket.send(ObjectMapper().writeValueAsString(applicationDTO))
            message = zeroMQReqSocket.recv()
        }
        return message?.let { mapper.readValue<RPCResponse>(it.decodeToString()) }
    }
}
