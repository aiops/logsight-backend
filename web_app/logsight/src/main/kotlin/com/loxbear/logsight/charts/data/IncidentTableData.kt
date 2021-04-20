package com.loxbear.logsight.charts.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class IncidentTableData(
    @JsonProperty("count_ads")
    val countAds: List<String>,

    @JsonProperty("semantic_count_ads")
    val semanticCountAds: List<String>,

    @JsonProperty("new_templates")
    val newTemplates: List<String>,

    @JsonProperty("semantic_ad")
    val semanticAd: List<String>,
)
