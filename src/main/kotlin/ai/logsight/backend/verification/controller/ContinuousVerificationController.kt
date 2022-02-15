package ai.logsight.backend.verification.controller

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.verification.controller.request.LogCompareRequest
import ai.logsight.backend.verification.dto.VerificationDTO
import ai.logsight.backend.verification.service.ContinuousVerificationService
import io.swagger.annotations.Api
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Api(tags = ["Applications"], description = " ")
@RestController
@RequestMapping("/api/v1/verification")
class ContinuousVerificationController(
    val applicationStorageService: ApplicationStorageService,
    val verificationService: ContinuousVerificationService
) {

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun trainLogCompareModel(@Valid @RequestBody logCompareRequest: LogCompareRequest): String {
        val application = applicationStorageService.findApplicationById(logCompareRequest.applicationId)
        val verificationDTO = VerificationDTO(
            applicationName = application.name,
            baselineTag = logCompareRequest.baselineTag,
            compareTag = logCompareRequest.compareTag,
            privateKey = application.user.key
        )
        return verificationService.getVerificationData(verificationDTO)
    }
}
