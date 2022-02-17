package ai.logsight.backend.compare.controller

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.compare.controller.request.GetCompareResultRequest
import ai.logsight.backend.compare.controller.response.CompareDataResponse
import ai.logsight.backend.compare.dto.CompareDTO
import ai.logsight.backend.compare.service.CompareService
import io.swagger.annotations.Api
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@Api(tags = ["Applications"], description = " ")
@RestController
@RequestMapping("/api/v1/compare")
class CompareController(
    val compareService: CompareService,
    val applicationStorageService: ApplicationStorageService
) {

    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun getCompareResults(@Valid @RequestBody getCompareResultRequest: GetCompareResultRequest): CompareDataResponse {
        val application = applicationStorageService.findApplicationById(getCompareResultRequest.applicationId)
        val compareDTO = CompareDTO(
            applicationId = application.id,
            applicationName = application.name,
            resultInitId = getCompareResultRequest.resultInitId,
            baselineTag = getCompareResultRequest.baselineTag,
            compareTag = getCompareResultRequest.compareTag,
            privateKey = application.user.key
        )
        val compareResponse = compareService.getCompareData(compareDTO)
        compareResponse.applicationId = application.id
        compareResponse.resultInitId = getCompareResultRequest.resultInitId
        return compareResponse
    }

    @PostMapping("/view")
    @ResponseStatus(HttpStatus.OK)
    fun getCompareViewResults(@Valid @RequestBody getCompareResultRequest: GetCompareResultRequest): String {
        val application = applicationStorageService.findApplicationById(getCompareResultRequest.applicationId)
        val compareDTO = CompareDTO(
            applicationId = application.id,
            applicationName = application.name,
            resultInitId = getCompareResultRequest.resultInitId,
            baselineTag = getCompareResultRequest.baselineTag,
            compareTag = getCompareResultRequest.compareTag,
            privateKey = application.user.key
        )
        return compareService.getCompareDataView(compareDTO)
    }

    @GetMapping("/versions")
    @ResponseStatus(HttpStatus.OK)
    fun getCompareVersions(
        @RequestParam(required = true) applicationId: UUID,
        @RequestParam(required = true) userId: UUID
    ): MutableList<String> {
        return compareService.getCompareTags(userId, applicationId) // use response here
    }
}
