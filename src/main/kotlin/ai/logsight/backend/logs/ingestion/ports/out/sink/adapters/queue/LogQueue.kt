package ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.queue

import ai.logsight.backend.logs.ingestion.exceptions.LogQueueCapacityLimitReached
import org.springframework.stereotype.Component
import java.util.concurrent.LinkedBlockingQueue
import javax.transaction.Transactional

@Component
class LogQueue(
    val blockingLogQueue: LinkedBlockingQueue<String>
) {
    @Transactional
    fun addAll(logs: Collection<String>) {
        if (logs.size > blockingLogQueue.remainingCapacity()) {
            throw LogQueueCapacityLimitReached(
                "log queue capacity limit reached. required capacity: ${logs.size}. " +
                    "remaining capacity: ${blockingLogQueue.remainingCapacity()}."
            )
        }
        blockingLogQueue.addAll(logs)
    }

    fun take(): String {
        return blockingLogQueue.take()
    }
}
