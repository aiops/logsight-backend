package ai.logsight.backend.compare.controller.request

data class TagValueRequest(
    val tagName: String,
    val indexType: String = "*",
    val listTags: List<TagEntry> = mutableListOf(),
)
