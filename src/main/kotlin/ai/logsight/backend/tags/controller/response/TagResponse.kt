package ai.logsight.backend.compare.controller.request

import ai.logsight.backend.compare.dto.TagKey

data class TagResponse(
    val tagKeys: List<TagKey> = listOf<TagKey>()
)
