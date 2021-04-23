package com.loxbear.logsight.charts.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class IncidentTableData(
    private val count_ads: List<String> = listOf(),

    private val semantic_count_ads: List<String> = listOf(),

    private val new_templates: List<String> = listOf(),

    private val semantic_ad: List<String> = listOf()
) {
    val countAds: List<String>
        get() = count_ads

    val semanticCountAds: List<String>
        get() = semantic_count_ads

    val newTemplates: List<String>
        get() = new_templates

    val semanticAd: List<String>
        get() = semantic_ad
}

