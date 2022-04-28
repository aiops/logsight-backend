package ai.logsight.backend.logs.ingestion.ports.out.stream

import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.ingestion.exceptions.LogQueueCapacityLimitReached
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import javax.transaction.Transactional

open class LogQueue(maxSize: Int) {
    val queue: BlockingQueue<Pair<String, LogsightLog>>

    init {
        queue = LinkedBlockingQueue(maxSize)
    }

    @Transactional
    open fun addAll(topic: String, logs: Collection<LogsightLog>) {
        if (logs.size > queue.remainingCapacity()) {
            throw LogQueueCapacityLimitReached(
                "log queue capacity limit reached. required capacity: ${logs.size}. " +
                    "remaining capacity: ${queue.remainingCapacity()}."
            )
        }
        queue.addAll(logs.map { Pair(topic, it) })
    }

    fun take(): Pair<String, LogsightLog> = queue.take()
}
