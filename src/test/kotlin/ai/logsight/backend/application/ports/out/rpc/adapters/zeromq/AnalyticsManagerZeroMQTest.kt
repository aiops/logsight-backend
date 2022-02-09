package ai.logsight.backend.application.ports.out.rpc.adapters.zeromq

import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.extensions.toApplication
import ai.logsight.backend.application.extensions.toApplicationDTO
import ai.logsight.backend.application.ports.out.persistence.ApplicationEntity
import ai.logsight.backend.application.ports.out.rpc.adapters.repsponse.RPCResponse
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTO
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTOActions
import ai.logsight.backend.connectors.zeromq.config.ZeroMQConfigurationProperties
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import ai.logsight.backend.users.ports.out.persistence.UserType
import com.fasterxml.jackson.module.kotlin.readValue
import com.sun.mail.iap.ConnectionException
import kotlinx.coroutines.newFixedThreadPoolContext
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
internal class AnalyticsManagerZeroMQTest {
    @Autowired
    lateinit var zeroMqConf: ZeroMQConfigurationProperties
    @Autowired
    lateinit var analyticsManagerZeroMQ: AnalyticsManagerZeroMQ

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
        private val application1 = applicationEntity1.toApplication()

        private val successResponse = RPCResponse("Success", HttpStatus.OK.value())
        private val failResponse = RPCResponse("Failed", HttpStatus.INTERNAL_SERVER_ERROR.value())

        // The IDE warning can be ignored
        private val threadPoolContext = newFixedThreadPoolContext(20, "ZeroMQ RPC")
    }

    private fun getZeroMqRepTestSocket(): ZMQ.Socket {
        val ctx = ZContext()
        val zeroMQSocket = ctx.createSocket(SocketType.REP)
        val addr = "${zeroMqConf.protocol}://${zeroMqConf.host}:${zeroMqConf.reqPort}"
        val status = zeroMQSocket.bind(addr)
        if (!status) throw ConnectionException("Test ZeroMQ REP socket is not able to connect to $addr")
        return zeroMQSocket
    }

    @Nested
    @DisplayName("Send RPC via ZeroMQ")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class SendZeroMqRPC {

        @Test
        fun `should transmit`() {
            // given
            val appDTO = application1.toApplicationDTO()
            appDTO.action = ApplicationDTOActions.CREATE
            val zeroMQRepSocket = getZeroMqRepTestSocket()

            // when
            val rpcResponse = analyticsManagerZeroMQ.sendZeroMqRPC(appDTO)
            val receivedAppDTOSerialized = zeroMQRepSocket.recv()
            val receivedAppDTO = analyticsManagerZeroMQ.mapper.readValue<ApplicationDTO>(
                receivedAppDTOSerialized.decodeToString()
            )
            val replySendStatus = zeroMQRepSocket.send(analyticsManagerZeroMQ.mapper.writeValueAsString(successResponse))
            zeroMQRepSocket.close()

            // then
        }
    }
}
