package ai.logsight.backend.charts.repository.builders

import ai.logsight.backend.charts.repository.ESQuery
import java.nio.file.Files
import java.nio.file.Paths

class ESQueryBuilder {
    fun buildQuery(startTime: String, stopTime: String, featureType: String, chartType: String): String {
        return this.loadJsonQuery(featureType, chartType).modifyTime(startTime, stopTime).query
    }

    private fun loadJsonQuery(featureType: String, chartType: String): ESQuery {
        val path = QueryPathBuilder().buildPath(featureType, chartType)
        return ESQuery(String(Files.readAllBytes(Paths.get(path.toString()))))
    }
}
