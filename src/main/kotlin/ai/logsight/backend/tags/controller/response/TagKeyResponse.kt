package ai.logsight.backend.compare.controller.request

import ai.logsight.backend.compare.dto.TagKey

data class TagKeyResponse(
    val tagKeys: List<TagKey> = listOf<TagKey>()
)
