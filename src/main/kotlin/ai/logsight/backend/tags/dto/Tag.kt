package ai.logsight.backend.compare.dto

data class Tag(
    val tagName: String,
    val tagValue: String,
    val tagCount: Long = 0
)
