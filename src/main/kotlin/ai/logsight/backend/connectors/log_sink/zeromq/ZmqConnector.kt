package ai.logsight.backend.connectors.log_sink.zeromq

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.connectors.log_sink.LogSinkConnector
import org.springframework.stereotype.Service
import org.zeromq.ZMQ
import org.zeromq.ZMQException

@Service
class ZmqConnector(
    val zmqSocket: ZMQ.Socket
) : LogSinkConnector {
    private val logger = LoggerImpl(ZmqConnector::class.java)

    override fun send(msg: String): Boolean {
        logger.debug("Sending message $msg to endpoint ${zmqSocket.lastEndpoint}.")
        val success = try {
            zmqSocket.send(msg)
        } catch (ex: ZMQException) {
            logger.warn("ZMQException exception raised while sending message $msg. Reason: $ex")
            false
        }
        if (!success) {
            logger.warn("Message transmission error. Zeromq socket was not able to queue the message $msg.")
        }
        return success
    }
}
