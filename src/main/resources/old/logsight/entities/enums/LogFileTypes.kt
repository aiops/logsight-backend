package com.loxbear.logsight.entities.enums

enum class LogFileTypes(val frontEndDescriptor: String) {
    SYSLOG("Syslog"),
    UNKNOWN_FORMAT("Other")
}
