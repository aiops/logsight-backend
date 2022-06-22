package ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.queued_zmq

import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.exceptions.LogQueueCapacityLimitReached
import ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.LogSinkAdapter
import org.springframework.stereotype.Component
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@Component
class BlockingQueueLogSinkAdapter(
    val blockingLogQueue: LinkedBlockingQueue<LogBatchDTO>,
) : LogSinkAdapter {
    fun take(): LogBatchDTO {
        return blockingLogQueue.take()
    }

    override fun sendBatch(logBatchDTO: LogBatchDTO) {
        if (!blockingLogQueue.offer(logBatchDTO, 100, TimeUnit.MILLISECONDS)) {
            throw LogQueueCapacityLimitReached(
                "log queue capacity limit reached. required capacity: 1, " +
                        "remaining capacity: ${blockingLogQueue.remainingCapacity()}."
            )
        }
    }
}
