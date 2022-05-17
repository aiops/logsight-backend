package ai.logsight.backend.connectors.sink.kafka

import ai.logsight.backend.connectors.sink.kafka.config.KafkaProducerConfigProperties
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
@DirtiesContext
internal class KafkaProducerSinkConnectorIntegrationTest {

    @Autowired
    lateinit var producer: KafkaProducerSinkConnector

    @Autowired
    lateinit var kafkaProducerConfigProperties: KafkaProducerConfigProperties

    lateinit var records: BlockingQueue<ConsumerRecord<String, String>>

    lateinit var container: KafkaMessageListenerContainer<String, String>

    @Nested
    @DisplayName("Send Logs via kafka")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class SendLogs {

        @BeforeAll
        fun setUp() {
            val configs: Map<String, Any> =
                HashMap(
                    KafkaTestUtils.consumerProps(
                        kafkaProducerConfigProperties.bootstrapServer,
                        "test", "true"
                    )
                )
            val consumerFactory: DefaultKafkaConsumerFactory<String, String> =
                DefaultKafkaConsumerFactory(configs, StringDeserializer(), StringDeserializer())
            val containerProperties = ContainerProperties(kafkaProducerConfigProperties.topic)
            container = KafkaMessageListenerContainer(consumerFactory, containerProperties)
            records = LinkedBlockingQueue()
            container.setupMessageListener(object : MessageListener<String, String> {
                override fun onMessage(msg: ConsumerRecord<String, String>) {
                    records.add(msg)
                }
            })
            container.start()
            ContainerTestUtils.waitForAssignment(container, kafkaProducerConfigProperties.partitions)
        }

        @AfterAll
        fun tearDown() {
            container.stop()
        }

        @Test
        fun `should send log to kafka success`() {
            // given
            val msg = "Hello world"

            // when
            val success = producer.send(msg)

            // then
            Assertions.assertTrue(success)
        }

        @Test
        fun `test transmission via kafka`() {
            // given
            val numMsg = 1000
            val msg = "Hello world"
            val messages = List(numMsg) { msg }

            // when
            val successes = messages.map {
                producer.send(it)
            }
            producer.kafkaTemplate.flush()

            // then
            Assertions.assertEquals(successes.size, numMsg)
            verifyZeroMqTransmission(messages)
        }

        private fun verifyZeroMqTransmission(sentMessages: List<String>) {
            val receivedMessages = List(sentMessages.size) { records.take() }

            // num. sent logs = num. received logs
            Assertions.assertEquals(receivedMessages.size, sentMessages.size)
            // Assertions.assertEquals(sentMessages, receivedMessages)
        }
    }
}
