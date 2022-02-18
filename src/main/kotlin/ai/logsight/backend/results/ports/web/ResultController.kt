package ai.logsight.backend.results.ports.web

import ai.logsight.backend.common.logging.Logger
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.logs.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.results.domain.ResultOperations
import ai.logsight.backend.results.domain.service.ResultService
import ai.logsight.backend.results.domain.service.command.CreateResultInitCommand
import ai.logsight.backend.results.ports.web.request.CreateResultInitRequest
import ai.logsight.backend.results.ports.web.response.CreateResultInitResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Api(tags = ["Control"], description = "Control operations on the data stream")
@RestController
@RequestMapping("/api/v1/logs/flush")
class ResultController(
    private val receiptStorageService: LogsReceiptStorageService,
    private val resultService: ResultService
) {

    private val logger: Logger = LoggerImpl(ResultController::class.java)

    /**
     * Register a new ResultInit object.
     */
    @ApiOperation("Flush currently sent logs in analysis pipeline")
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun createResultInit(
        @Valid @RequestBody createResultInitRequest: CreateResultInitRequest
    ): CreateResultInitResponse {
        val logsReceipt = receiptStorageService.findLogsReceiptById(createResultInitRequest.receiptId)
        val application = logsReceipt.application
        val user = application.user
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
            flushId = resultInit.id, status = resultInit.status
        )
    }
}
