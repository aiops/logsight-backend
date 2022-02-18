package ai.logsight.backend.charts.repository.builders

import ai.logsight.backend.charts.repository.ESQuery
import ai.logsight.backend.charts.exceptions.InvalidFeatureException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.exists

class ESQueryBuilder {
    fun buildQuery(startTime: String, stopTime: String, featureType: String, chartType: String): String {
        return this.loadJsonQuery(featureType, chartType).modifyTime(startTime, stopTime).query
    }

    private fun loadJsonQuery(featureType: String, chartType: String): ESQuery {
        val fileInputStream = QueryPathBuilder().buildPath(featureType, chartType)
        return fileInputStream?.let { fileInputStreamNotNull ->
            ESQuery(String(fileInputStreamNotNull.readAllBytes()))
        } ?: throw InvalidFeatureException("Feature $featureType does not exist for chart $chartType.")
    }
}
