package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq

data class Log(
    val app_name: String,
    val private_key: String,
    val message: String
)
