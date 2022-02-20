//package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq
//
//import org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.integration.zeromq.inbound.ZeroMqMessageProducer
//import org.zeromq.ZMQ
//
//internal class AnalyticsManagerLogSinkZeroMQTest {
//    @Test
//    fun sendLogs() {
//        val zeromq = AnalyticsManagerLogSinkZeroMQ()
//        for (i in 1..3) {
//            zeromq.sendLogs("$i")
//        }
//    }
//
//    @Test
//    fun `zeromq 2`() {
//        // given
//        val context = ZMQ.context(1)
//
//        val socket = context.socket(ZMQ.REQ)
//        println("connecting to hello world server...")
//        socket.connect("tcp://localhost:5897")
//
//        for (i in 1..10) {
//            var plainRequest = "Hello "
//
//            var byteRequest = plainRequest.toByteArray()
//
//            byteRequest[byteRequest.size - 1] = 0
//
//            println("sending request $i $plainRequest")
//            socket.send(byteRequest, 0)
//
////            val byteReply = socket.recv(0)
//
////            var plainReply = String(byteReply, 0, byteReply.size - 1)
//
////            println("Received reply $plainReply")
//
//            // when
//
//            // then
//        }
//    }
//}
