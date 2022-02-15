package ai.logsight.backend.results.ports.web

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.common.logging.Logger
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.logs.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.results.domain.service.ResultService
import ai.logsight.backend.results.domain.service.command.CreateResultInitCommand
import ai.logsight.backend.results.ports.web.request.CreateResultInitRequest
import ai.logsight.backend.results.ports.web.response.CreateResultInitResponse
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@Api(tags = ["Results"], description = " ")
@RestController
@RequestMapping("/api/v1/users/{userId}/applications/{applicationId}")
class ResultController(
    private val userStorageService: UserStorageService,
    private val applicationStorageService: ApplicationStorageService,
    private val receiptStorageService: LogsReceiptStorageService,
    private val resultService: ResultService
) {

    private val logger: Logger = LoggerImpl(ResultController::class.java)

    /**
     * Register a new ResultInit object.
     */
    @ApiOperation("Create ResultInit")
    @PostMapping("/result_init")
    @ResponseStatus(HttpStatus.CREATED)
    fun createResultInit(
        @PathVariable userId: UUID,
        @PathVariable applicationId: UUID,
        @Valid @RequestBody createResultInitRequest: CreateResultInitRequest
    ): CreateResultInitResponse {
        val user = userStorageService.findUserById(userId)
        val application = applicationStorageService.findApplicationById(applicationId)
        val logsReceipt = receiptStorageService.findLogsReceiptById(createResultInitRequest.receiptId)
        val createResultInitCommand = CreateResultInitCommand(
            user = user,
            application = application,
            logsReceipt = logsReceipt
        )
        logger.info("Creating ResultInit object for logs receipt $logsReceipt.", this::createResultInit.name)
        val resultInit = resultService.createResultInit(createResultInitCommand)
        logger.info(
            "ResultInit with id: ${resultInit.id} successfully created.",
            this::createResultInit.name
        )
        return CreateResultInitResponse(
            id = resultInit.id, status = resultInit.status, logsReceipt = resultInit.logsReceipt
        )
    }
}
