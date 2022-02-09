package ai.logsight.backend.application.ports.out.rpc.adapters.zeromq

import ai.logsight.backend.application.ports.out.rpc.AnalyticsManagerRPC
import ai.logsight.backend.application.ports.out.rpc.adapters.repsponse.RPCResponse
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTO
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.zeromq.ZMQ

@Service
@Qualifier("ZeroMQ")
class AnalyticsManagerZeroMQ(
    @Qualifier("req") val zeroMQReqSocket: ZMQ.Socket
) : AnalyticsManagerRPC {

    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    override fun createApplication(createApplicationDTO: ApplicationDTO): RPCResponse? {
        createApplicationDTO.action = "CREATE"
        zeroMQReqSocket.send(mapper.writeValueAsString(createApplicationDTO))
        val message = zeroMQReqSocket.recv()
        return message?.let { mapper.readValue<RPCResponse>(message.decodeToString()) }
    }

    override fun deleteApplication(deleteApplicationDTO: ApplicationDTO): RPCResponse? {
        deleteApplicationDTO.action = "DELETE"
        zeroMQReqSocket.send(ObjectMapper().writeValueAsString(deleteApplicationDTO))
        val message = zeroMQReqSocket.recv()
        return message?.let { mapper.readValue<RPCResponse>(message.decodeToString()) }
    }
}
