package ai.logsight.backend.results.ports.web

import ai.logsight.backend.common.logging.Logger
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.results.domain.service.ResultService
import ai.logsight.backend.results.domain.service.command.CreateResultInitCommand
import ai.logsight.backend.results.domain.service.query.FindResultInitQuery
import ai.logsight.backend.results.ports.web.request.CreateFlushRequest
import ai.logsight.backend.results.ports.web.response.FlushResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

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
    fun createFlush(
        @Valid @RequestBody createFlushRequest: CreateFlushRequest
    ): FlushResponse {
        val logsReceipt = receiptStorageService.findLogsReceiptById(createFlushRequest.receiptId)
        val application = logsReceipt.application
        val user = application.user
        val createResultInitCommand = CreateResultInitCommand(
            user = user,
            application = application,
            logsReceipt = logsReceipt
        )
        logger.info("Creating ResultInit object for logs receipt $logsReceipt.", this::createFlush.name)
        val resultInit = resultService.createResultInit(createResultInitCommand)
        logger.info(
            "ResultInit with id: ${resultInit.id} successfully created.",
            this::createFlush.name
        )
        return FlushResponse(
            flushId = resultInit.id, status = resultInit.status
        )
    }

    @ApiOperation("Get flush object by flushId")
    @GetMapping("/{flushId}")
    @ResponseStatus(HttpStatus.OK)
    fun getFlush(
        @Valid
        @NotNull(message = "flushId must not be empty.")
        @Pattern(
            regexp = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",
            message = "flushId must be UUID type."
        )
        @PathVariable flushId: String
    ): FlushResponse {
        val resultInit = resultService.findResultInit(FindResultInitQuery(UUID.fromString(flushId)))
        return FlushResponse(
            flushId = resultInit.id, status = resultInit.status
        )
    }
}
