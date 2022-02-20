package ai.logsight.backend.compare.controller

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.common.config.CommonConfigProperties
import ai.logsight.backend.compare.controller.request.GetCompareResultRequest
import ai.logsight.backend.compare.controller.response.CompareDataResponse
import ai.logsight.backend.compare.dto.CompareDTO
import ai.logsight.backend.compare.dto.Tag
import ai.logsight.backend.compare.service.CompareService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.util.*
import javax.validation.Valid

@Api(tags = ["Compare"], description = "Comparison between log data")
@RestController
@RequestMapping("/api/v1/logs/compare")
class CompareController(
    val compareService: CompareService,
    val applicationStorageService: ApplicationStorageService,
    val commonConfigProperties: CommonConfigProperties
) {

    @ApiOperation("Obtain log compare results between two tags")
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun getCompareResults(@Valid @RequestBody getCompareResultRequest: GetCompareResultRequest): CompareDataResponse {

        val application = applicationStorageService.findApplicationById(getCompareResultRequest.applicationId)
        val compareDTO = CompareDTO(
            applicationId = application.id,
            applicationName = application.name,
            resultInitId = getCompareResultRequest.flushId,
            baselineTag = getCompareResultRequest.baselineTag,
            compareTag = getCompareResultRequest.candidateTag,
            privateKey = application.user.key
        )
        val compareResponse = compareService.getCompareData(compareDTO)
        compareResponse.applicationId = application.id
        compareResponse.flushId = getCompareResultRequest.flushId
        compareResponse.link =
            "${commonConfigProperties.baseURL}/pages/compare?applicationId=${application.id}&baselineTag=${getCompareResultRequest.baselineTag}&compareTag=${getCompareResultRequest.candidateTag}"
        return compareResponse
    }

    @ApiIgnore
    @PostMapping("/view")
    @ResponseStatus(HttpStatus.OK)
    fun getCompareViewResults(@Valid @RequestBody getCompareResultRequest: GetCompareResultRequest): String {
        val application = applicationStorageService.findApplicationById(getCompareResultRequest.applicationId)
        val compareDTO = CompareDTO(
            applicationId = application.id,
            applicationName = application.name,
            resultInitId = getCompareResultRequest.flushId,
            baselineTag = getCompareResultRequest.baselineTag,
            compareTag = getCompareResultRequest.candidateTag,
            privateKey = application.user.key
        )
        return compareService.getCompareDataView(compareDTO)
    }

    @ApiOperation("Get all available tags for specific application")
    @GetMapping("/tags")
    @ResponseStatus(HttpStatus.OK)
    fun getCompareTags(
        @RequestParam(required = true) applicationId: UUID,
        @RequestParam(required = true) userId: UUID
    ): MutableList<Tag> {
        return compareService.getCompareTags(userId, applicationId) // use response here
    }
}
