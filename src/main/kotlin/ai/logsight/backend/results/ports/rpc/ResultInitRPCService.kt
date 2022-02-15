package ai.logsight.backend.results.ports.rpc

import ai.logsight.backend.results.ports.rpc.dto.FlushDTO

interface ResultInitRPCService {
    fun flush(topic: String, flushDTO: FlushDTO)
}
