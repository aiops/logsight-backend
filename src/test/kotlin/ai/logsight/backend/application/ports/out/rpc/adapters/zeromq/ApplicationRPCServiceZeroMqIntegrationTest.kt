package ai.logsight.backend.application.ports.out.rpc.adapters.zeromq

import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.exceptions.ApplicationRemoteException
import ai.logsight.backend.application.extensions.toApplication
import ai.logsight.backend.application.extensions.toApplicationDTO
import ai.logsight.backend.application.ports.out.persistence.ApplicationEntity
import ai.logsight.backend.application.ports.out.rpc.adapters.repsponse.RPCResponse
import ai.logsight.backend.application.ports.out.rpc.adapters.zeromq.config.ApplicationRPCConfigPropertiesZeroMq
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTO
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTOActions
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import ai.logsight.backend.users.ports.out.persistence.UserType
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
@DirtiesContext
internal class ApplicationRPCServiceZeroMqIntegrationTest {
    @Autowired
    lateinit var zeroMqConf: ApplicationRPCConfigPropertiesZeroMq

    @Autowired
    lateinit var applicationRPCServiceZeroMq: ApplicationRPCServiceZeroMq

    companion object {
        private val userEntity = UserEntity(
            email = "testemail@mail.com",
            password = "testpassword",
            userType = UserType.ONLINE_USER

        )
        private val applicationEntity1 = ApplicationEntity(
            name = "testapp1",
            status = ApplicationStatus.READY,
            user = userEntity
        )
        private val application = applicationEntity1.toApplication()
        private val appDTO = application.toApplicationDTO()

        private val successResponse = RPCResponse(appDTO.id.toString(), "Success", HttpStatus.OK.value())

        private val threadPoolContext = newFixedThreadPoolContext(20, "ZeroMQ RPC")
    }

    private fun getZeroMqRepTestSocket(): ZMQ.Socket {
        val ctx = ZContext()
        val zeroMQSocket = ctx.createSocket(SocketType.REP)
        val addr = "${zeroMqConf.protocol}://${zeroMqConf.host}:${zeroMqConf.port}"
        // Sometimes the socket ends up in a state where close is called but the address is still in use
        // Calling bind() again resolves this
        for (i in 1..5) {
            try {
                if (zeroMQSocket.bind(addr)) break
            } catch (_: Exception) {
                continue
            }
        }
        Thread.sleep(5)
        return zeroMQSocket
    }

    @Nested
    @DisplayName("Send RPC via ZeroMQ")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class SendZeroMqRPC {

        @Test
        fun `should successfully execute RPC`() {
            // given
            appDTO.action = ApplicationDTOActions.CREATE

            // when
            val rpcTestResult = executeApplicationRPCTest(appDTO, successResponse)

            // then
            assertEquals(successResponse, rpcTestResult.rpcResponse)
            assertEquals(appDTO, rpcTestResult.receivedAppDTO)
            assertEquals(appDTO.id.toString(), rpcTestResult.rpcResponse?.id)
            assertTrue(rpcTestResult.replySendStatus)
        }

        @Test
        fun `should successfully execute RPC concurrent`() {
            // given
            val testSize = 1000
            appDTO.action = ApplicationDTOActions.CREATE
            val appDTOs = List(1000) { appDTO }
            val zeroMQRepSocket = getZeroMqRepTestSocket()

            // then
            var respResults: List<Boolean>? = null
            val responses = Collections.synchronizedList(mutableListOf<Pair<ApplicationDTO, RPCResponse>>())
            runBlocking(threadPoolContext) {
                launch {
                    respResults = appDTOs.map {
                        zeroMQRepSocket.recv()
                        zeroMQRepSocket.send(applicationRPCServiceZeroMq.mapper.writeValueAsString(successResponse))
                    }
                }
                appDTOs.forEach { appDTO ->
                    launch {
                        val rpcResponse = applicationRPCServiceZeroMq.sendZeroMqRPC(appDTO)
                        responses.add(Pair(appDTO, rpcResponse))
                    }
                }
            }
            zeroMQRepSocket.close()

            // then
            Assertions.assertTrue { respResults?.all { it } ?: false }
            Assertions.assertEquals(testSize, responses.size)
            Assertions.assertTrue { responses.all { it.second.status.is2xxSuccessful } }
        }

        @Test
        fun `execute RPC concurrent and check req and resp match`() {
            // given
            val testSize = 1000
            appDTO.action = ApplicationDTOActions.CREATE
            val appDTOs = IntArray(testSize) { it }.map {
                ApplicationDTO(UUID.randomUUID(), it.toString(), it.toString(), "key", ApplicationDTOActions.CREATE)
            }
            val zeroMQRepSocket = getZeroMqRepTestSocket()

            // then
            val responses = Collections.synchronizedList(mutableListOf<Pair<ApplicationDTO, RPCResponse>>())
            runBlocking(threadPoolContext) {
                launch {
                    appDTOs.forEach { _ ->
                        val receivedAppDTOSerialized = zeroMQRepSocket.recv()
                        val appDTOReceived = applicationRPCServiceZeroMq.mapper.readValue<ApplicationDTO>(
                            receivedAppDTOSerialized.decodeToString()
                        )
                        val resp = RPCResponse(appDTOReceived.id.toString(), appDTOReceived.name, HttpStatus.OK.value())
                        zeroMQRepSocket.send(applicationRPCServiceZeroMq.mapper.writeValueAsString(resp))
                    }
                }
                appDTOs.forEach { appDTO ->
                    launch {
                        val rpcResponse = applicationRPCServiceZeroMq.sendZeroMqRPC(appDTO)
                        responses.add(Pair(appDTO, rpcResponse))
                    }
                }
            }
            zeroMQRepSocket.close()

            // Name and message field are abused to verify that request and response match
            val expected: Array<String> = responses.map { it.first.name }
                .toTypedArray()
            val actual: Array<String> = responses.map { it.second.message }
                .toTypedArray()

            // then
            Assertions.assertArrayEquals(expected, actual)
        }

        @Test
        fun `RPC call should timeout`() {
            assertThrows<ApplicationRemoteException> {
                applicationRPCServiceZeroMq.sendZeroMqRPC(appDTO)
            }
        }

        @Test
        fun `send after timeout`() {
            // given
            appDTO.action = ApplicationDTOActions.CREATE

            // when
            assertThrows<ApplicationRemoteException> {
                applicationRPCServiceZeroMq.sendZeroMqRPC(appDTO)
            }
            val rpcTestResult = executeApplicationRPCTest(appDTO, successResponse)

            assertEquals(successResponse, rpcTestResult.rpcResponse)
            assertEquals(appDTO, rpcTestResult.receivedAppDTO)
            assertEquals(appDTO.id.toString(), rpcTestResult.rpcResponse?.id)
            assertTrue(rpcTestResult.replySendStatus)
        }

        @Test
        fun `send after timeout for different apps`() {
            // given
            val appDTOTimeout = ApplicationDTO(
                UUID.randomUUID(), "test", "test", "key", ApplicationDTOActions.CREATE
            )
            appDTO.action = ApplicationDTOActions.CREATE

            // when
            assertThrows<ApplicationRemoteException> {
                applicationRPCServiceZeroMq.sendZeroMqRPC(appDTOTimeout)
            }
            val rpcTestResult = executeApplicationRPCTest(appDTO, successResponse)

            assertEquals(successResponse, rpcTestResult.rpcResponse)
            assertEquals(appDTO, rpcTestResult.receivedAppDTO)
            assertEquals(appDTO.id.toString(), rpcTestResult.rpcResponse?.id)
            assertTrue(rpcTestResult.replySendStatus)
        }
    }

    fun executeApplicationRPCTest(appDTO: ApplicationDTO, expectedResponse: RPCResponse): RPCTestResult {
        val zeroMQRepSocket = getZeroMqRepTestSocket()
        val rpcTestResult = RPCTestResult()
        runBlocking(threadPoolContext) {
            launch {
                val receivedAppDTOSerialized = zeroMQRepSocket.recv()
                rpcTestResult.receivedAppDTO = applicationRPCServiceZeroMq.mapper.readValue(
                    receivedAppDTOSerialized.decodeToString()
                )
                rpcTestResult.replySendStatus =
                    zeroMQRepSocket.send(applicationRPCServiceZeroMq.mapper.writeValueAsString(expectedResponse))
            }
            rpcTestResult.rpcResponse = applicationRPCServiceZeroMq.sendZeroMqRPC(appDTO)
        }
        zeroMQRepSocket.close()

        return rpcTestResult
    }

    inner class RPCTestResult(
        var rpcResponse: RPCResponse? = null,
        var receivedAppDTO: ApplicationDTO? = null,
        var replySendStatus: Boolean = false
    )
}
