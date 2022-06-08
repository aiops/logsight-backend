package ai.logsight.backend.common.logging

import org.slf4j.LoggerFactory

class LoggerImpl(kClass: Class<*>) : Logger {

    private val logger: org.slf4j.Logger = LoggerFactory.getLogger(kClass)

    override fun info(msg: String, methodName: String) {
        logger.info("$methodName:: $msg")
    }

    override fun error(msg: String, methodName: String) {
        logger.error("$methodName:: $msg")
    }

    override fun warn(msg: String, methodName: String) {
        logger.warn("$methodName:: $msg")
    }

    override fun debug(msg: String, methodName: String) {
        logger.debug("$methodName:: $msg")
    }
}
