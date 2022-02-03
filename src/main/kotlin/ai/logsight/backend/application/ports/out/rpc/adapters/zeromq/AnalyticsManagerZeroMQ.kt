package ai.logsight.backend.application.ports.out.rpc.adapters.zeromq

import ai.logsight.backend.application.ports.out.rpc.AnalyticsManagerRPC
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTO
import org.zeromq.SocketType
import org.zeromq.ZContext

class AnalyticsManagerZeroMQ: AnalyticsManagerRPC {
    override fun createApplication(createApplicationDTO: ApplicationDTO) {
        val ctx = ZContext()
        val zeroMqPubSocket = ctx.createSocket(SocketType.REQ)
        val adr = "tcp://0.0.0.0:5554"
        zeroMqPubSocket.bind(adr)
        zeroMqPubSocket.send(createApplicationDTO.toString())
        val message = zeroMqPubSocket.recv()
        TODO("log that app creation is successfull")
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