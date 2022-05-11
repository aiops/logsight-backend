package ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.queue

import ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.zeromq.ZeroMqSink
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component

@Component
final class QueuedLogStreamTransmission(
    private val logQueue: LogQueue,
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
            val log = logQueue.take()
            sink.sendString(log)
        }
    }

    override fun destroy() {
        stopReading = true
    }
}
