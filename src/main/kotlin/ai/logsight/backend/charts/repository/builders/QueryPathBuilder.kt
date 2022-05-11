package ai.logsight.backend.charts.repository.builders

import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class QueryPathBuilder {
    fun buildPath(featureType: String, chartType: String): InputStream? {
        return QueryPathBuilder::class.java.classLoader.getResourceAsStream(
            "queries/$chartType/$featureType.json"
        )
    }
}
