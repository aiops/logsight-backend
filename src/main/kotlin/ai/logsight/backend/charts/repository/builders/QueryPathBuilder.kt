package ai.logsight.backend.charts.repository.builders

import org.springframework.stereotype.Component
import java.nio.file.Path
import java.nio.file.Paths

@Component
class QueryPathBuilder {
    private var resourcesPath: String = "src/main/resources/"

    // TODO: 25.01.22 extract this in app.properties.yaml
    fun buildPath(featureType: String, chartType: String): Path {
        return Paths.get("${resourcesPath}queries/$chartType/$featureType.json")
    }
}
