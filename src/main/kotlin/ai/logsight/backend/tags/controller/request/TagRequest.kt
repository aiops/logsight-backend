package ai.logsight.backend.compare.controller.request

data class TagRequest(
    val listTags: List<TagEntry> = mutableListOf(),
    val indexType: String = "*"
)
