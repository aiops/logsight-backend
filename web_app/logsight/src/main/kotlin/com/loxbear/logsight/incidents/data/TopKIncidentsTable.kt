package com.loxbear.logsight.incidents.data

data class TopKIncidentsTable(
    val labels: List<String>,
    val datasets: List<DataSet>
)