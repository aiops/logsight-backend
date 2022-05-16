package ai.logsight.backend.logs.ingestion.ports.out.log_sink.config

import com.fasterxml.jackson.annotation.JsonProperty

enum class LogSinkConnectorTypes {
    @JsonProperty("zmq")
    ZMQ,

    @JsonProperty("queued-zmq")
    QUEUED_ZMQ,

    @JsonProperty("kafka")
    KAFKA,
}