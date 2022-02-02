package ai.logsight.backend.logs.domain.service

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.logs.domain.LogDTO
import ai.logsight.backend.logs.domain.service.helpers.TopicBuilder
import ai.logsight.backend.logs.ports.out.stream.LogStream
import ai.logsight.backend.logs.ports.out.stream.adapters.zeromq.Log
import ai.logsight.backend.users.domain.service.UserService
import ai.logsight.backend.users.domain.service.query.FindUserByEmailQuery
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class LogsServiceImpl(
    val userService: UserService,
    val applicationStorageService: ApplicationStorageService,
    val topicBuilder: TopicBuilder,
    val logStream: LogStream,
) : LogsService {
    val log: Logger = Logger.getLogger(LogsService::class.java.toString())

    override fun forwardLogs(logDTO: LogDTO) {
        val app = applicationStorageService.findApplicationById(logDTO.appName)
        val user = userService.findUserByEmail(FindUserByEmailQuery(logDTO.email))
        val topic = topicBuilder.buildTopic(user.key, app.name)
        val logs = logDTO.logs.map { msg -> Log(app.name, user.key, msg) }
        logStream.send(topic, logs)
    }

    private fun processFileContent(
        fileContent: String
    ): List<String> {
        val logMessages = mutableListOf<String>()
        var buffer = ""
        fileContent.lines().filter { it.isNotEmpty() }.forEach {
            buffer = if (it.first().isWhitespace()) {
                buffer.plus(it.trim().plus(" "))
            } else {
                if (buffer.isNotEmpty()) logMessages.add(buffer.trim())
                it.trim().plus(" ")
            }
        }
        if (buffer.isNotEmpty()) {
            logMessages.add(buffer.trim())
        }
        return logMessages
    }
}
