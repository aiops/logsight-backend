package ai.logsight.backend.tags.controller

import ai.logsight.backend.compare.controller.request.TagKeyResponse
import ai.logsight.backend.compare.controller.request.TagRequest
import ai.logsight.backend.compare.controller.request.TagValueRequest
import ai.logsight.backend.compare.controller.request.TagValueResponse
import ai.logsight.backend.tags.service.TagService
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Api(tags = ["Tags"], description = "Operations with log data tags")
@RestController
@RequestMapping("/api/v1/logs/tags")
class TagController(
    val tagService: TagService,
    val userStorageService: UserStorageService,
) {
    @ApiOperation("Get all available tag values for a specific tag name")
    @PostMapping("/values")
    @ResponseStatus(HttpStatus.OK)
    fun getCompareTagValues(
        authentication: Authentication,
        @RequestBody tagValueRequest: TagValueRequest
    ): TagValueResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        return TagValueResponse(
            tagValues = tagService.getCompareTagValues(
                user,
                tagValueRequest.tagName,
                tagValueRequest.indexType,
                tagValueRequest.listTags
            )
        )
    }

    @ApiOperation("Get all available tags given selected tags")
    @PostMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    fun getCompareTags(
        authentication: Authentication,
        @RequestBody(required = false) tagRequest: TagRequest = TagRequest()
    ): TagKeyResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        return TagKeyResponse(tagKeys = tagService.getCompareTagFilter(user, tagRequest.listTags, tagRequest.indexType))
    }
}
