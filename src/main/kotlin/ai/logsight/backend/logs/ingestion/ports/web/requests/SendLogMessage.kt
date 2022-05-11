package ai.logsight.backend.logs.ingestion.ports.web.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class SendLogMessage(
    @get:Pattern(
        regexp = "^[a-zA-Z0-9][a-zA-Z0-9_.-]+\$",
        message = "applicationName must follow the following regex pattern ^[a-zA-Z0-9][a-zA-Z0-9_.-]+\\\$."
    )
    val applicationName: String? = null,

    val applicationId: UUID? = null,

    @get:NotEmpty(message = "tag must not be empty")
    val tags: Map<String, String> = mapOf("default" to "default"),

    @get:Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(([+-]\\d{2}:\\d{2})|Z)?\$",
        message = "timestamp must be defined as ISO timestamp " +
            "YYYY-MM-DDTHH:mm:ss.SSSSSS+HH:00. If timezone is not specified, UTC is default."
    )
    @get:NotEmpty(message = "timestamp must not be null or empty")
    val timestamp: String,
    val message: String,
    @get:Pattern(
        regexp = "^INFO$|^WARNING$|^WARN$|^FINER$|^FINE$|^DEBUG$|^ERROR$|^ERR$|^EXCEPTION$|^SEVERE$",
        message = "level must be one of INFO|WARNING|WARN|FINE|FINER|DEBUG|ERR|ERROR|EXCEPTION|SEVERE"
    )
    val level: String? = null,
    val metadata: Map<String, String>? = null,
) {
    @AssertTrue(message = "One of applicationId or applicationName must not be empty")
    fun isValid(): Boolean {
        return Objects.nonNull(this.applicationName) || Objects.nonNull(this.applicationId)
    }
}
