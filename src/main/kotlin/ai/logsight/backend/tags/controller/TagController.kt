package ai.logsight.backend.compare.controller

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.compare.controller.request.TagKeyResponse
import ai.logsight.backend.compare.controller.request.TagRequest
import ai.logsight.backend.compare.controller.request.TagValueResponse
import ai.logsight.backend.compare.dto.TagKey
import ai.logsight.backend.compare.service.TagService
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@Api(tags = ["Compare"], description = "Comparison between log data")
@RestController
@RequestMapping("/api/v1/logs/tags")
class TagController(
    val tagService: TagService,
    val applicationStorageService: ApplicationStorageService,
    val userStorageService: UserStorageService,
) {

    @ApiOperation("Get all available tag values for a specific tag name.")
    @PostMapping("/values")
    @ResponseStatus(HttpStatus.OK)
    fun getCompareTagValues(
        authentication: Authentication,
        @RequestBody tagName: String
    ): TagValueResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        return TagValueResponse(tagValues = tagService.getCompareTagValues(user, tagName, "*"))
    }

    @ApiOperation("Get all available tags for specific application, given selected tags.")
    @PostMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    fun getCompareTags(
        authentication: Authentication,
        @RequestBody(required = false) tagRequest: TagRequest = TagRequest()
    ): TagKeyResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        val tagData = tagService.getCompareTagFilter(user, tagRequest.listTags, "*")
        return TagKeyResponse(
            tagKeys = tagData.aggregations.listAggregations.buckets.filter { itFilter ->
                !tagRequest.listTags.map { itMap1 -> itMap1.tagName }.contains(itFilter.tagValue)
            }.map { itMap2 -> TagKey(tagName = itMap2.tagValue, itMap2.tagCount) }
        )
    }
}
