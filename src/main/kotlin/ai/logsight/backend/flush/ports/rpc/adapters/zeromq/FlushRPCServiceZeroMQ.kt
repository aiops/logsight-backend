package ai.logsight.backend.flush.ports.rpc.adapters.zeromq

import ai.logsight.backend.common.logging.Logger
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.common.utils.TopicJsonSerializer
import ai.logsight.backend.flush.ports.rpc.FlushRPCService
import ai.logsight.backend.flush.ports.rpc.dto.FlushDTO
import com.antkorwin.xsync.XSync
import org.springframework.stereotype.Service
import org.zeromq.ZMQ

@Service
class FlushRPCServiceZeroMQ(
    val flushRPCSocketPub: ZMQ.Socket,
    val serializer: TopicJsonSerializer,
    val xSync: XSync<String>
) : FlushRPCService {

    private val logger: Logger = LoggerImpl(FlushRPCServiceZeroMQ::class.java)

    override fun flush(topic: String, flushDTO: FlushDTO) {
        // zeromq is generally not thread save --> sending needs to be synchronized
        xSync.execute("resul-init-rpc") { // TODO Move mutex definitions to somewhere else
            logger.info(
                "Sending RPC request via ZeroMQ for Flush object ${flushDTO.id} to logsight core.",
                this::flush.name
            )
            flushRPCSocketPub.send(serializer.serialize(topic, flushDTO))
        }
    }
}
