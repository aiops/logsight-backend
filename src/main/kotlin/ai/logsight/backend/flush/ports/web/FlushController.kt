package ai.logsight.backend.flush.ports.web

import ai.logsight.backend.common.logging.Logger
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.flush.domain.service.FlushService
import ai.logsight.backend.flush.domain.service.command.CreateFlushCommand
import ai.logsight.backend.flush.domain.service.query.FindFlushQuery
import ai.logsight.backend.flush.ports.web.request.CreateFlushRequest
import ai.logsight.backend.flush.ports.web.response.FlushResponse
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
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
class FlushController(
    private val receiptStorageService: LogsReceiptStorageService,
    private val flushService: FlushService
) {

    private val logger: Logger = LoggerImpl(FlushController::class.java)

    /**
     * Register a new Flush object.
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
        val createFlushCommand = CreateFlushCommand(
            user = user,
            application = application,
            logsReceipt = logsReceipt
        )
        logger.info("Creating Flush object for logs receipt $logsReceipt.", this::createFlush.name)
        val flush = flushService.createFlush(createFlushCommand)
        logger.info(
            "Flush with id: ${flush.id} successfully created.",
            this::createFlush.name
        )
        return FlushResponse(
            flushId = flush.id, status = flush.status
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
        val flush = flushService.findFlush(FindFlushQuery(UUID.fromString(flushId)))
        return FlushResponse(
            flushId = flush.id, status = flush.status
        )
    }
}
