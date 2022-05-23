package ai.logsight.backend.compare.ports.web

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.common.config.CommonConfigProperties
import ai.logsight.backend.compare.ports.web.request.GetCompareResultRequest
import ai.logsight.backend.compare.ports.web.response.CompareDataResponse
import ai.logsight.backend.compare.domain.dto.CompareDTO
import ai.logsight.backend.compare.domain.dto.Tag
import ai.logsight.backend.compare.domain.service.CompareService
import ai.logsight.backend.compare.controller.request.GetCompareResultRequest
import ai.logsight.backend.compare.controller.response.CompareDataResponse
import ai.logsight.backend.compare.dto.CompareDTO
import ai.logsight.backend.compare.service.CompareService
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import javax.naming.AuthenticationException
import javax.validation.Valid

@Api(tags = ["Compare"], description = "Comparison between log data")
@RestController
@RequestMapping("/api/v1/logs/compare")
class CompareController(
    val compareService: CompareService,
    val applicationStorageService: ApplicationStorageService,
    val userStorageService: UserStorageService,
    val commonConfigProperties: CommonConfigProperties
) {

    @ApiOperation("Obtain log compare results between two tags")
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun createCompare(
        authentication: Authentication,
        @Valid @RequestBody getCompareResultRequest: GetCompareResultRequest
    ): CompareDataResponse {
        val application = if (getCompareResultRequest.applicationName == null) {
            applicationStorageService.findApplicationById(getCompareResultRequest.applicationId)
        } else {
            applicationStorageService.findApplicationByUserAndName(userStorageService.findUserByEmail(authentication.name), getCompareResultRequest.applicationName)
        }
        if (application.user.email != authentication.name) {
            throw AuthenticationException("Unauthorized")
        }
        val compareDTO = CompareDTO(
            applicationId = application.id,
            applicationName = application.name,
            logsReceiptId = getCompareResultRequest.logsReceiptId,
            baselineTag = getCompareResultRequest.baselineTag,
            compareTag = getCompareResultRequest.candidateTag,
            flushId = getCompareResultRequest.flushId,
            baselineTags = getCompareResultRequest.baselineTags,
            candidateTags = getCompareResultRequest.candidateTags,
            privateKey = application.user.key
        )
        val compareResponse = compareService.getCompareData(compareDTO)
        compareResponse.applicationId = application.id
        compareResponse.logsReceiptId = getCompareResultRequest.logsReceiptId
        compareResponse.flushId = getCompareResultRequest.flushId
        var baselineTags = ""
        for ((key, value) in getCompareResultRequest.baselineTags.entries) {
            baselineTags += "&baselineTag:$key=$value"
        }
        var candidateTags = ""
        for ((key, value) in getCompareResultRequest.baselineTags.entries) {
            candidateTags += "&baselineTag:$key=$value"
        }
        compareResponse.link =
            "${commonConfigProperties.baseURL}/pages/compare?applicationId=${application.id}${baselineTags}$candidateTags"
        compareResponse.baselineTags = getCompareResultRequest.baselineTags
        compareResponse.candidateTags = getCompareResultRequest.candidateTags
        return compareResponse
    }

    @ApiIgnore
    @PostMapping("/view")
    @ResponseStatus(HttpStatus.OK)
    fun getCompareViewResults(
        @Valid @RequestBody getCompareResultRequest: GetCompareResultRequest
    ): String {
        val application = applicationStorageService.findApplicationById(getCompareResultRequest.applicationId)
        val compareDTO = CompareDTO(
            applicationId = application.id,
            applicationName = application.name,
            logsReceiptId = getCompareResultRequest.logsReceiptId,
            baselineTag = getCompareResultRequest.baselineTag,
            compareTag = getCompareResultRequest.candidateTag,
            flushId = getCompareResultRequest.flushId,
            baselineTags = getCompareResultRequest.baselineTags,
            candidateTags = getCompareResultRequest.candidateTags,
            privateKey = application.user.key
        )
        return compareService.getCompareDataView(compareDTO)
    }
}
