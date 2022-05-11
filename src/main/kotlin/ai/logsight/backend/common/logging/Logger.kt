package ai.logsight.backend.common.logging

interface Logger {
    fun info(msg: String, methodName: String = "methodNotSpecified")
    fun error(msg: String, methodName: String = "methodNotSpecified")
    fun warn(msg: String, methodName: String = "methodNotSpecified")
    fun debug(msg: String, methodName: String = "methodNotSpecified")
}
