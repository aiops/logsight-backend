package ai.logsight.backend.logs.ingestion.ports.out.sink.config

import com.fasterxml.jackson.annotation.JsonProperty

enum class LogSinkTypes {
    @JsonProperty("zmq")
    ZMQ,

    @JsonProperty("queued-zmq")
    QUEUED_ZMQ,

    @JsonProperty("kafka")
    KAFKA,
}
