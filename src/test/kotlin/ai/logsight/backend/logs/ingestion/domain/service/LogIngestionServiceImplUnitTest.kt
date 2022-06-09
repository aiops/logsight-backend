package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.logs.extensions.toLogBatchDTO
import ai.logsight.backend.logs.ingestion.domain.LogReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogEventsDTO
import ai.logsight.backend.logs.ingestion.domain.dto.LogListDTO
import ai.logsight.backend.logs.ingestion.domain.enums.LogBatchStatus
import ai.logsight.backend.logs.ingestion.ports.out.exceptions.LogSinkException
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogReceiptStorageService
import ai.logsight.backend.logs.ingestion.ports.out.sink.LogSink
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

internal class LogIngestionServiceImplUnitTest {

    private val logSink: LogSink = mockk(relaxed = true)
    private val logReceiptStorageService: LogReceiptStorageService = mockk(relaxed = true)
    private val ingestionService = LogIngestionServiceImpl(logReceiptStorageService, logSink)

    companion object {
        private val logBatch = TestInputConfig.logBatch
        private val logReceipt = LogReceipt(
            id = UUID.randomUUID(), logsCount = logBatch.logs.size, batchId = logBatch.id,
            processedLogCount = 0, status = LogBatchStatus.PROCESSING
        )
    }

    @Test
    fun processLogBatch() {
        every { logReceiptStorageService.saveLogReceipt(any()) } returns logReceipt

        // when
        val receiptResult = ingestionService.processLogBatch(logBatch)

        // then
        Assertions.assertEquals(receiptResult, logReceipt)
        verify(exactly = 1) { logSink.sendLogBatch(logBatch.toLogBatchDTO()) }
    }

    @Test
    fun `should set receipt to failed on LogSinkException`() {
        // given
        every { logReceiptStorageService.saveLogReceipt(any()) } returns logReceipt
        every { logSink.sendLogBatch(any()) } throws LogSinkException()

        // when
        Assertions.assertThrows(LogSinkException::class.java) { ingestionService.processLogBatch(logBatch) }
        // then
        verify(exactly = 1) { logSink.sendLogBatch(logBatch.toLogBatchDTO()) }
        verify(exactly = 1) { logReceiptStorageService.deleteLogReceipt(any()) }
    }

    @Test
    fun processLogList() {
        val logListDTO = LogListDTO(
            index = TestInputConfig.baseUser.key,
            logs = List(TestInputConfig.numMessages) { TestInputConfig.logEvent }
        )

        every { logReceiptStorageService.saveLogReceipt(any()) } returns logReceipt

        // when
        val result = ingestionService.processLogList(logListDTO)
        Assertions.assertEquals(result.logsCount, logReceipt.logsCount)
        Assertions.assertEquals(result.processedLogCount, logReceipt.processedLogCount)
        verify(exactly = 1) { logSink.sendLogBatch(any()) }
    }

    @Test
    fun processLogEvents() {
        every { logReceiptStorageService.saveLogReceipt(any()) } returns logReceipt
        val logEventsDTO = LogEventsDTO(
            index = TestInputConfig.baseUser.key,
            logs = List(TestInputConfig.numMessages) { TestInputConfig.sendLogMessage }
        )

        val result = ingestionService.processLogEvents(logEventsDTO)

        Assertions.assertEquals(result.logsCount, logReceipt.logsCount)
        Assertions.assertEquals(result.processedLogCount, logReceipt.processedLogCount)
        verify(exactly = 1) { logSink.sendLogBatch(any()) }
    }
}
