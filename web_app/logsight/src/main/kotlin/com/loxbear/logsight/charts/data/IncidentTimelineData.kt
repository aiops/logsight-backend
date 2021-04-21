package com.loxbear.logsight.charts.data

data class IncidentTimelineData(
    val key: String,
    val values: List<IncidentTimeline>,
    val bar: Boolean = true
)
