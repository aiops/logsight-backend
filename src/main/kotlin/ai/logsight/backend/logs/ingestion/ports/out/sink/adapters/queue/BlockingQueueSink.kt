package ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.queue

import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.exceptions.LogQueueCapacityLimitReached
import ai.logsight.backend.logs.ingestion.ports.out.sink.Sink
import org.springframework.stereotype.Component
import java.util.concurrent.LinkedBlockingQueue

@Component
class BlockingQueueSink(
    val blockingLogQueue: LinkedBlockingQueue<LogBatchDTO>,
) : Sink {
    fun take(): LogBatchDTO {
        return blockingLogQueue.take()
    }

    override fun sendBatch(logBatchDTO: LogBatchDTO) {
        if (blockingLogQueue.remainingCapacity() < 1) {
            throw LogQueueCapacityLimitReached(
                "log queue capacity limit reached. required capacity: 1, " +
                    "remaining capacity: ${blockingLogQueue.remainingCapacity()}."
            )
        }
        blockingLogQueue.put(logBatchDTO)
    }
}
