package ai.logsight.backend.results.ports.rpc.adapters.zeromq

import ai.logsight.backend.common.logging.Logger
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.common.utils.TopicJsonSerializer
import ai.logsight.backend.results.ports.rpc.ResultInitRPCService
import ai.logsight.backend.results.ports.rpc.dto.FlushDTO
import com.antkorwin.xsync.XSync
import org.springframework.stereotype.Service
import org.zeromq.ZMQ

@Service
class ResultInitRPCServiceZeroMQ(
    val resultInitRPCSocketPub: ZMQ.Socket,
    val serializer: TopicJsonSerializer,
    val xSync: XSync<String>
) : ResultInitRPCService {

    private val logger: Logger = LoggerImpl(ResultInitRPCServiceZeroMQ::class.java)

    override fun flush(topic: String, flushDTO: FlushDTO) {
        // zeromq is generally not thread save --> sending needs to be synchronized
        xSync.execute("resul-init-rpc") { // TODO Move mutex definitions to somewhere else
            logger.info(
                "Sending RPC request via ZeroMQ for ResultInit object ${flushDTO.id} to logsight core.",
                this::flush.name
            )
            resultInitRPCSocketPub.send(serializer.serialize(topic, flushDTO))
        }
    }
}
