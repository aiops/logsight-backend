package ai.logsight.backend.compare.controller

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.charts.repository.entities.elasticsearch.TagData
import ai.logsight.backend.compare.controller.request.TagEntry
import ai.logsight.backend.compare.controller.request.TagRequest
import ai.logsight.backend.compare.controller.request.TagResponse
import ai.logsight.backend.compare.dto.Tag
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
    ): List<Tag> {
        val user = userStorageService.findUserByEmail(authentication.name)
        return tagService.getCompareTagValues(user, tagName) // use response here
    }

    @ApiOperation("Get all available tags for specific application, given selected tags.")
    @PostMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    fun getCompareTags(
        authentication: Authentication,
        @RequestBody(required = false) tagRequest: TagRequest = TagRequest()
    ): TagResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        val tagData = tagService.getCompareTagFilter(user, tagRequest.listTags)
        return TagResponse(tagKeys = tagData.aggregations.listAggregations.buckets.filter { !tagRequest.listTags.map { it.tagName }.contains(it.tagValue) }.map { TagKey(tagName = it.tagValue, it.tagCount) })
    }
}
