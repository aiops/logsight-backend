package ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.queued_zmq

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.logs.ingestion.exceptions.LogQueueCapacityLimitReached
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.concurrent.LinkedBlockingQueue
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class BlockingQueueLogStreamUnitTestSink {

    val blockingQueueSink = BlockingQueueLogSinkAdapter(LinkedBlockingQueue(100))

    @Nested
    @DisplayName("Logs")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ProcessLogs {

        @BeforeEach
        fun setup() {
            blockingQueueSink.blockingLogQueue.clear()
        }

        @Test
        fun `should add all elements to the queue`() {
            // given
            val numBatches = 50
            // when
            for (i in 1..numBatches) {
                blockingQueueSink.sendBatch(TestInputConfig.logBatchDTO)
            }
            // then
            kotlin.test.assertEquals(numBatches, blockingQueueSink.blockingLogQueue.size)
        }

        @Test
        fun `should add all elements to the queue edge case`() {
            // given
            val numBatches = blockingQueueSink.blockingLogQueue.remainingCapacity()
            // when
            for (i in 1..numBatches) {
                blockingQueueSink.sendBatch(TestInputConfig.logBatchDTO)
            }
            // then
            kotlin.test.assertEquals(numBatches, blockingQueueSink.blockingLogQueue.size)
        }

        @Test
        fun `should get all elements from the queue`() {
            // given
            val numBatches = 50
            val expected = List(numBatches) { TestInputConfig.logBatchDTO }
            for (i in 1..numBatches) {
                blockingQueueSink.sendBatch(TestInputConfig.logBatchDTO)
            }
            // when
            val queuedLogBatchesDTO = List(numBatches) { blockingQueueSink.take() }
            // then
            assertEquals(expected, queuedLogBatchesDTO)
        }

        @Test
        fun `should fail due to capacity limit`() {
            // given
            val numBatches = blockingQueueSink.blockingLogQueue.remainingCapacity() + 1
            // when
            val exception = assertFailsWith<LogQueueCapacityLimitReached> {
                for (i in 1..numBatches) {
                    blockingQueueSink.sendBatch(TestInputConfig.logBatchDTO)
                }
            }
            // then
            assertNotNull(exception)
        }
    }
}
