package com.loxbear.logsight.charts.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHit

@JsonIgnoreProperties(ignoreUnknown = true)
class IncidentTableData(
        private val count_ads: List<VariableAnalysisHit> = listOf(),

        private val semantic_count_ads: List<VariableAnalysisHit> = listOf(),

        private val new_templates: List<VariableAnalysisHit> = listOf(),

        private val semantic_ad: List<VariableAnalysisHit> = listOf(),

        private val logs: List<VariableAnalysisHit> = listOf()
) {
    val countAds: List<VariableAnalysisHit>
        get() = count_ads

    val semanticCountAds: List<VariableAnalysisHit>
        get() = semantic_count_ads

    val newTemplates: List<VariableAnalysisHit>
        get() = new_templates

    var semanticAd: List<VariableAnalysisHit> = listOf()
        get() = semantic_ad

    var logData: List<VariableAnalysisHit> = listOf()
        get() = logs
}

