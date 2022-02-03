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
        zeroMQReqSocket.send(ObjectMapper().writeValueAsString(createApplicationDTO))
        val message = zeroMQReqSocket.recv()
        println(message)
    }

    override fun deleteApplication(deleteApplicationDTO: ApplicationDTO) {
        val ctx = ZContext()
        val zeroMqPubSocket = ctx.createSocket(SocketType.REQ)
        val adr = "tcp://0.0.0.0:5554"
        zeroMqPubSocket.bind(adr)
        zeroMqPubSocket.send(deleteApplicationDTO.toString())
        val message = zeroMqPubSocket.recv()
        TODO("log that app creation is successfull")
    }
}
