package ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.queue

import ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.zeromq.ZeroMqSink
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component

@Component
final class QueuedLogStreamTransmission(
    private val blockingQueueSink: BlockingQueueSink,
    private val sink: ZeroMqSink
) : DisposableBean, Runnable {
    @Volatile
    private var stopReading = false

    private val thread: Thread = Thread(this)

    init {
        thread.start()
    }

    override fun run() {
        while (!stopReading) {
            val logBatchDTO = blockingQueueSink.take()
            sink.sendBatch(logBatchDTO)
        }
    }

    override fun destroy() {
        stopReading = true
    }
}
