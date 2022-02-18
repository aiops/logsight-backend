package ai.logsight.backend.logs.domain.enums

enum class LogDataSources(val source: String) {
    REST_BATCH("restBatch"),
    FILE("file"),
    SAMPLE("sample")
}
