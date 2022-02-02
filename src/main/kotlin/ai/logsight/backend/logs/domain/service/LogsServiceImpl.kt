package ai.logsight.backend.logs.domain.service

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.logs.domain.service.command.LogCommand
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

    override fun forwardLogs(logCommand: LogCommand) {
        val app = applicationStorageService.findApplicationById(logCommand.applicationId)
        val user = userService.findUserByEmail(FindUserByEmailQuery(logCommand.userEmail))
        val topic = topicBuilder.buildTopic(user.key, app.name)
        val logs = logCommand.logs.map { message ->
            Log(app.name, user.key, logCommand.logFormat.toString(), logCommand.tag, message)
        }
        logStream.send(topic, logs)
    }
}
