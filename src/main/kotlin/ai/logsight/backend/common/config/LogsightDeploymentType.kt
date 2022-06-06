package ai.logsight.backend.common.config

import com.fasterxml.jackson.annotation.JsonProperty

enum class LogsightDeploymentType {
    @JsonProperty("stand-alone")
    STAND_ALONE,

    @JsonProperty("web-service")
    WEB_SERVICE,
}
