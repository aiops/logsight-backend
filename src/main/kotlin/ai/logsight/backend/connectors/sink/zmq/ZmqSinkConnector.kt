package ai.logsight.backend.connectors.sink.zmq

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.connectors.sink.SinkConnector
import org.springframework.stereotype.Service
import org.zeromq.ZMQ
import org.zeromq.ZMQException

@Service
class ZmqSinkConnector(
    val zmqSocket: ZMQ.Socket
) : SinkConnector {
    private val logger = LoggerImpl(ZmqSinkConnector::class.java)

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
