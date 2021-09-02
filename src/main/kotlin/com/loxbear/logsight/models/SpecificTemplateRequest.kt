package com.loxbear.logsight.models

data class SpecificTemplateRequest(
    var template: String,
    val param: String,
    val paramValue: String
)