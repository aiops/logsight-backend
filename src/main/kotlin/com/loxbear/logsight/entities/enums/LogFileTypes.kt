package com.loxbear.logsight.entities.enums

enum class LogFileTypes(val frontEndDescriptor: String) {
    LOGSIGHT_JSON("JSON - native"),
    SYSLOG("Syslog"),
    UNKNOWN_FORMAT("Other")
}
