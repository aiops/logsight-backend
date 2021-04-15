package com.loxbear.logsight.models

data class SpecificTemplateRequest(
    val template: String,
    val param: String,
    val paramValue: String
)