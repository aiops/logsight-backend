package ai.logsight.backend.application.ports.out.rpc.adapters.zeromq

import ai.logsight.backend.application.ports.out.rpc.AnalyticsManagerRPC
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTO
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

@Service
@Qualifier("ZeroMQ")
class AnalyticsManagerZeroMQ(
    @Qualifier("req") val zeroMQReqSocket: ZMQ.Socket
) : AnalyticsManagerRPC {
    override fun createApplication(createApplicationDTO: ApplicationDTO) {
        createApplicationDTO.action = "CREATE"
        zeroMQReqSocket.send(ObjectMapper().writeValueAsString(createApplicationDTO))
        val message = zeroMQReqSocket.recv()
        println(message.toString())
    }

    override fun deleteApplication(deleteApplicationDTO: ApplicationDTO) {
        deleteApplicationDTO.action = "DELETE"
        zeroMQReqSocket.send(ObjectMapper().writeValueAsString(deleteApplicationDTO))
        val message = zeroMQReqSocket.recv()
        println(message.toString())
    }
}
