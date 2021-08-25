package com.loxbear.logsight.entities.enums

enum class LogFileTypes(val frontEndDescriptor: String) {
    LOGSIGHT_JSON("Native json"),
    SYSLOG("Syslog");
}
