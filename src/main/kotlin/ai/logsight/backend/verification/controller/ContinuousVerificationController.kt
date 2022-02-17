package ai.logsight.backend.verification.controller

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.verification.controller.request.GetVerificationResultRequest
import ai.logsight.backend.verification.dto.VerificationDTO
import ai.logsight.backend.verification.service.ContinuousVerificationService
import io.swagger.annotations.Api
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@Api(tags = ["Applications"], description = " ")
@RestController
@RequestMapping("/api/v1/verification")
class ContinuousVerificationController(
    val verificationService: ContinuousVerificationService,
    val applicationStorageService: ApplicationStorageService
) {

    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun getVerificationResults(@Valid @RequestBody getVerificationResultRequest: GetVerificationResultRequest): String {
        val application = applicationStorageService.findApplicationById(getVerificationResultRequest.applicationId)
        val verificationDTO = VerificationDTO(
            applicationId = application.id,
            applicationName = application.name,
            resultInitId = getVerificationResultRequest.resultInitId,
            baselineTag = getVerificationResultRequest.baselineTag,
            compareTag = getVerificationResultRequest.compareTag,
            privateKey = application.user.key
        )
        return verificationService.getVerificationData(verificationDTO)
    }

    @GetMapping("/versions")
    @ResponseStatus(HttpStatus.OK)
    fun getVersions(
        @RequestParam(required = true) applicationId: UUID,
        @RequestParam(required = true) userId: UUID
    ): MutableList<String> {
        return verificationService.getVerificationTags(userId, applicationId) // use response here
    }
}
