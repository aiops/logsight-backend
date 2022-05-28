package ai.logsight.backend.compare.ports.web

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.common.config.CommonConfigProperties
import ai.logsight.backend.compare.domain.dto.CompareDTO
import ai.logsight.backend.compare.domain.service.CompareService
import ai.logsight.backend.compare.ports.web.request.GetCompareAnalyticsIssueKPIRequest
import ai.logsight.backend.compare.ports.web.request.GetCompareResultRequest
import ai.logsight.backend.compare.ports.web.request.UpdateCompareStatusRequest
import ai.logsight.backend.compare.ports.web.response.*
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
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

    @ApiOperation("Create log compare between baseline and candidate tags")
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun createCompare(
        authentication: Authentication,
        @Valid @RequestBody getCompareResultRequest: GetCompareResultRequest
    ): CompareDataResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        val compareDTO = CompareDTO(
            logsReceiptId = getCompareResultRequest.logsReceiptId,
            baselineTags = getCompareResultRequest.baselineTags,
            candidateTags = getCompareResultRequest.candidateTags,
            privateKey = user.key
        )
        val compareResponse = compareService.getCompareData(compareDTO)
        compareResponse.link =
            "${commonConfigProperties.baseURL}/pages/compare?compareId=${compareResponse.compareId}"
        compareResponse.baselineTags = getCompareResultRequest.baselineTags
        compareResponse.candidateTags = getCompareResultRequest.candidateTags
        return compareResponse
    }

    @ApiOperation("Get compare by ID")
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun getCompareByID(
        authentication: Authentication,
        @RequestParam(required = false) compareId: String? = null,
    ): GetCompareByIdResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        return GetCompareByIdResponse(compareService.getCompareByID(compareId, user))
    }

    @ApiOperation("Delete compare by ID")
    @DeleteMapping("{compareId}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteCompareByID(
        authentication: Authentication,
        @PathVariable compareId: String,
    ): DeleteCompareByIdResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        return DeleteCompareByIdResponse(compareService.deleteCompareByID(compareId, user))
    }

    @ApiOperation("Update compare status by ID")
    @PostMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    fun updateCompareStatusByID(
        authentication: Authentication,
        @Valid @RequestBody updateCompareStatusRequest: UpdateCompareStatusRequest
    ): UpdateCompareStatusResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        return UpdateCompareStatusResponse(
            compareService.updateCompareStatusByID(
                updateCompareStatusRequest.compareId, updateCompareStatusRequest.compareStatus, user
            )
        )
    }

    @ApiOperation("Get analytics KPI for issues")
    @PostMapping("/issues")
    @ResponseStatus(HttpStatus.OK)
    fun getAnalyticsIssuesKPI(
        authentication: Authentication,
        @Valid @RequestBody getCompareAnalyticsIssueKPIRequest: GetCompareAnalyticsIssueKPIRequest
    ): CompareAnalyticsIssueKPIResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        return CompareAnalyticsIssueKPIResponse(compareService.getAnalyticsIssuesKPI(user, getCompareAnalyticsIssueKPIRequest.baselineTags))
    }
}
