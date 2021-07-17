package com.loxbear.logsight.models

import org.springframework.http.HttpStatus

data class Application (
    val description: String,
    val status: HttpStatus,
    val id: Long,
    val name: String){
}