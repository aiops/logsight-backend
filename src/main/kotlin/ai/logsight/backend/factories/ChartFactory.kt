package ai.logsight.backend.factories

import ai.logsight.backend.charts.ESChart
import ai.logsight.backend.charts.esdata.ESData

interface ChartFactory {
    fun createChart(): ESChart
    fun getData(): ESData
}
