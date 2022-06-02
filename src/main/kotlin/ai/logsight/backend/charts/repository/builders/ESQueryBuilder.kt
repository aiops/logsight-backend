package ai.logsight.backend.charts.repository.builders

import ai.logsight.backend.charts.repository.ESQuery
import ai.logsight.backend.charts.exceptions.InvalidFeatureException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.exists

class ESQueryBuilder {
    fun buildQuery(parameters: Map<String, String>): String {
        return this.loadJsonQuery(parameters["feature"].toString(), parameters["type"].toString()).modifyTime(
            parameters["startTime"].toString(), parameters["stopTime"].toString()
        ).modifyField(parameters["field"].toString()).query
    }

    private fun loadJsonQuery(featureType: String, chartType: String): ESQuery {
        val fileInputStream = QueryPathBuilder().buildPath(featureType, chartType)
        return fileInputStream?.let { fileInputStreamNotNull ->
            ESQuery(String(fileInputStreamNotNull.readAllBytes()))
        } ?: throw InvalidFeatureException("Feature $featureType does not exist for chart $chartType.")
    }
}
