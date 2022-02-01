package ai.logsight.backend.charts.repository.builders

import ai.logsight.backend.charts.repository.ESQuery
import ai.logsight.backend.exceptions.InvalidFeatureException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

class ESQueryBuilder {
    fun buildQuery(startTime: String, stopTime: String, featureType: String, chartType: String, timeZone: String): String {
        return this.loadJsonQuery(featureType, chartType).modifyTime(startTime, stopTime, timeZone).query
    }

    private fun loadJsonQuery(featureType: String, chartType: String): ESQuery {
        val path = QueryPathBuilder().buildPath(featureType, chartType)
        if (!path.exists()) {
            val msg = "Feature $featureType does not exist for chart $chartType."
            throw InvalidFeatureException(msg)
        }
        return ESQuery(String(Files.readAllBytes(Paths.get(path.toString()))))
    }
}
