package ai.logsight.backend.compare.controller.request

import ai.logsight.backend.compare.dto.Tag
import org.springframework.web.bind.annotation.RequestParam
import java.util.*
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class TagEntry(
    val tagName: String,
    val tagValue: String
)
