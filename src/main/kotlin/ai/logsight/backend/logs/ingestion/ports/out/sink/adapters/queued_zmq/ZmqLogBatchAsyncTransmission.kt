package ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.queued_zmq

import ai.logsight.backend.logs.ingestion.ports.out.exceptions.LogSinkException
import ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.zeromq.ZmqLogSinkAdapter
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component

@Component
final class ZmqLogBatchAsyncTransmission(
    private val blockingQueueSinkAdapter: BlockingQueueLogSinkAdapter,
    private val zeroMqLogStreamSink: ZmqLogSinkAdapter
) : DisposableBean, Runnable {
    @Volatile
    private var stopReading = false

    private val thread: Thread = Thread(this)

    init {
        thread.start()
    }

    override fun run() {
        while (!stopReading) {
            val logBatchDTO = blockingQueueSinkAdapter.take()
            try {
                zeroMqLogStreamSink.sendBatch(logBatchDTO)
            } catch (_: LogSinkException) {
                // Not much we can do here. The necessary information are already logged.
            }
        }
    }

    override fun destroy() {
        stopReading = true
    }
}
