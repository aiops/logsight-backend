package com.loxbear.logsight.models.log

class LogMessageException : Exception {
    constructor(message: String, ex: Exception?): super(message, ex)
    constructor(message: String): super(message)
    constructor(ex: Exception): super(ex)
}