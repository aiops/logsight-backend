package ai.logsight.backend.logs.ingestion.ports.out.stream

import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.ingestion.exceptions.LogQueueCapacityLimitReached
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.concurrent.BlockingQueue
import javax.transaction.Transactional

@Component
class LogQueue(
    val blockingLogQueue: BlockingQueue<Pair<String, LogsightLog>>
) {
    @Transactional
    fun addAll(topic: String, logs: Collection<LogsightLog>) {
        if (logs.size > blockingLogQueue.remainingCapacity()) {
            throw LogQueueCapacityLimitReached(
                "log queue capacity limit reached. required capacity: ${logs.size}. " +
                    "remaining capacity: ${blockingLogQueue.remainingCapacity()}."
            )
        }
        blockingLogQueue.addAll(logs.map { Pair(topic, it) })
    }

    fun take(): Pair<String, LogsightLog> {
        return blockingLogQueue.take()
    }
}
