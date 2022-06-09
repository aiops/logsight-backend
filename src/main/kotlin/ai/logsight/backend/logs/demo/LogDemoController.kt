package ai.logsight.backend.logs.demo

import ai.logsight.backend.logs.ingestion.ports.web.responses.LogReceiptResponse
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Api(tags = ["Logs"], description = "Upload sample logs for demo.")
@RestController
@RequestMapping("/api/v1/demo")
class LogDemoController(
    val logDemoService: LogDemoService,
    val userStorageService: UserStorageService
) {
    @ApiOperation("Load sample log data")
    @PostMapping("/hadoop")
    fun sampleData(authentication: Authentication): List<LogReceiptResponse> {
        val user = userStorageService.findUserByEmail(authentication.name)
        val logReceipts = logDemoService.createHadoopDemoForUser(user)
        return logReceipts.map { logReceipt ->
            LogReceiptResponse(
                logReceipt.id,
                logReceipt.logsCount,
                logReceipt.batchId,
                logReceipt.status
            )
        }
    }
}
