package ai.logsight.backend.compare.controller.request

import ai.logsight.backend.compare.dto.Tag

data class TagValueResponse(
    val tagValues: List<Tag> = listOf()
)
