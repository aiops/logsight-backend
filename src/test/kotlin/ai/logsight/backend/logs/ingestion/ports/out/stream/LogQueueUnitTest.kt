// package ai.logsight.backend.logs.ingestion.ports.out.stream
//
// import ai.logsight.backend.TestInputConfig.logsightLog
// import ai.logsight.backend.common.utils.TopicBuilder
// import ai.logsight.backend.logs.domain.LogsightLog
// import ai.logsight.backend.logs.ingestion.exceptions.LogQueueCapacityLimitReached
// import ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.queue.LogQueue
// import org.junit.Test
// import org.junit.jupiter.api.Assertions
// import org.junit.jupiter.api.BeforeEach
// import org.junit.jupiter.api.extension.ExtendWith
// import org.mockito.junit.jupiter.MockitoExtension
// import org.springframework.test.annotation.DirtiesContext
// import java.util.*
// import java.util.concurrent.LinkedBlockingQueue
// import kotlin.test.assertEquals
// import kotlin.test.assertFailsWith
// import kotlin.test.assertNotNull
//
// @ExtendWith(MockitoExtension::class)
// @DirtiesContext
// internal class LogQueueUnitTest {
//
//    private val blockedQueue = LinkedBlockingQueue<Pair<String, LogsightLog>>(100)
//    private val newBlockedQueue = LinkedBlockingQueue<String>(100)
//    private val logQueue = LogQueue(newBlockedQueue)
//
//    companion object {
//        private val topicBuilder = TopicBuilder()
//    }
//
//    @BeforeEach
//    fun setupEach() {
//        logQueue.blockingLogQueue.clear()
//    }
//
//    @Test
//    fun `should add all elements to the queue`() {
//        // given
//        val numLogs = 50
//        val logs = List(numLogs) { logsightLog }
//        val topic = topicBuilder.buildTopic(listOf(private_key, app_name))
//
//        // when
//        logQueue.addAll(topic, logs)
//
//        // then
//        assertEquals(numLogs, logQueue.blockingLogQueue.size)
//    }
//
//    @Test
//    fun `should add all elements to the queue edge case 100`() {
//        // given
//        val numLogs = 100
//        val logs = List(numLogs) { log }
//        val topic = topicBuilder.buildTopic(listOf(private_key, app_name))
//
//        // when
//        logQueue.addAll(topic, logs)
//
//        // then
//        assertEquals(numLogs, logQueue.blockingLogQueue.size)
//    }
//
//    @Test
//    fun `should get all elements from the queue`() {
//        // given
//        val numLogs = 50
//        val logs = List(numLogs) { log }
//        val topic = topicBuilder.buildTopic(listOf(private_key, app_name))
//        logQueue.addAll(topic, logs)
//
//        // when
//        val queuedLogs = List(numLogs) { logQueue.take() }
//
//        // then
//        Assertions.assertEquals(logs, queuedLogs.map { it.second })
//    }
//
//    @Test
//    fun `should fail due to capacity limit`() {
//        // given
//        val numLogs = 101
//        val logs = List(numLogs) { log }
//        val topic = topicBuilder.buildTopic(listOf(private_key, app_name))
//
//        // when
//        val exception = assertFailsWith<LogQueueCapacityLimitReached> {
//            logQueue.addAll(topic, logs)
//        }
//
//        // then
//        assertNotNull(exception)
//    }
// }
