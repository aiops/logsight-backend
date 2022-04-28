package ai.logsight.backend.logs.ingestion.ports.out.stream

import ai.logsight.backend.logs.ingestion.ports.out.stream.adapters.zeromq.LogStreamZeroMq
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component

@Component
final class QueuedLogStreamTransmission(
    private val logQueue: LogQueue,
    private val logStream: LogStreamZeroMq
) : DisposableBean, Runnable {
    @Volatile
    private var stopReading = false

    private val thread: Thread = Thread(this)

    init {
        thread.start()
    }

    override fun run() {
        while (!stopReading) {
            val topicAndLog = logQueue.take()
            println("${topicAndLog.first}: ${topicAndLog.second.message}")
            // logStream.serializeAndSendAll(topicAndLog.first, topicAndLog.second)
        }
    }

    override fun destroy() {
        stopReading = true
    }
}
