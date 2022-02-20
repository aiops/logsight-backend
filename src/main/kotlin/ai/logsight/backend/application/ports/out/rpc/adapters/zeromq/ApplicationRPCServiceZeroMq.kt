package ai.logsight.backend.application.ports.out.rpc.adapters.zeromq

import ai.logsight.backend.application.exceptions.ApplicationRemoteException
import ai.logsight.backend.application.ports.out.rpc.RPCService
import ai.logsight.backend.application.ports.out.rpc.adapters.repsponse.RPCResponse
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTO
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTOActions
import ai.logsight.backend.common.logging.LoggerImpl
import com.antkorwin.xsync.XSync
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.zeromq.ZMQ

@Service
@Primary
class ApplicationRPCServiceZeroMq(
    val zeroMqRPCSocket: ZMQ.Socket,
    val xSync: XSync<String>
) : RPCService {

    val mapper = ObjectMapper().registerModule(KotlinModule())!!
    private val logger = LoggerImpl(ApplicationRPCServiceZeroMq::class.java)

    override fun createApplication(createApplicationDTO: ApplicationDTO): RPCResponse {
        createApplicationDTO.action = ApplicationDTOActions.CREATE
        return sendZeroMqRPC(createApplicationDTO)
    }

    override fun deleteApplication(deleteApplicationDTO: ApplicationDTO): RPCResponse {
        deleteApplicationDTO.action = ApplicationDTOActions.DELETE
        return sendZeroMqRPC(deleteApplicationDTO)
    }

    fun sendZeroMqRPC(applicationDTO: ApplicationDTO): RPCResponse {
        var message: ByteArray? = null
        xSync.execute("logsight-rpc") { // TODO Move mutex definitions to somewhere else
            logger.info(
                "Sending RPC request via ZeroMQ for application ${applicationDTO.id} to logsight core.",
                this::sendZeroMqRPC.name
            )
            zeroMqRPCSocket.send(mapper.writeValueAsString(applicationDTO))
            var respId = ""
            while (applicationDTO.id.toString() != respId) {
                message = zeroMqRPCSocket.recv()
                respId = message?.let { mapper.readValue<RPCResponse>(it.decodeToString()).id } ?: break
            }
            logger.info(
                "Received RPC response via ZeroMQ for application ${applicationDTO.id} to logsight core.",
                this::sendZeroMqRPC.name
            )
        }
        return message?.let { mapper.readValue<RPCResponse>(it.decodeToString()) } ?: throw ApplicationRemoteException(
            "Timeout while waiting for RPC reply to ${applicationDTO.action} application ${applicationDTO.name}."
        )
    }
}
