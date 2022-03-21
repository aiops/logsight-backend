package ai.logsight.backend.flush.ports.rpc

import ai.logsight.backend.flush.ports.rpc.dto.FlushDTO

interface FlushRPCService {
    fun flush(topic: String, flushDTO: FlushDTO)
}
