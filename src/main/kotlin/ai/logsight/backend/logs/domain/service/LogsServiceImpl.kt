package ai.logsight.backend.logs.domain.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.logs.domain.LogFileTypes
import ai.logsight.backend.logs.domain.service.command.LogCommand
import ai.logsight.backend.logs.domain.service.helpers.TopicBuilder
import ai.logsight.backend.logs.ports.out.stream.LogStream
import ai.logsight.backend.logs.ports.out.stream.adapters.zeromq.Log
import ai.logsight.backend.logs.ports.web.requests.SendFileRequest
import ai.logsight.backend.users.domain.service.UserService
import ai.logsight.backend.users.domain.service.query.FindUserByEmailQuery
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File

@Service
class LogsServiceImpl(
    val userService: UserService,
    val applicationStorageService: ApplicationStorageService,
    val topicBuilder: TopicBuilder,
    val logStream: LogStream,
) : LogsService {
    val logger: org.slf4j.Logger = LoggerFactory.getLogger(LogsServiceImpl::class.java)

    @Value("\${resources.path}")
    private lateinit var resourcesPath: String

    override fun forwardLogs(logCommand: LogCommand) {
        val app = applicationStorageService.findApplicationById(logCommand.applicationId)
        val user = userService.findUserByEmail(FindUserByEmailQuery(logCommand.userEmail))
        val topic = topicBuilder.buildTopic(user.key, app.name)
        val logs = logCommand.logs.map { message ->
            Log(app.name, user.key, logCommand.logFormat.toString(), logCommand.tag, message)
        }
        logStream.send(topic, logs)
    }

    override fun processFile(logRequest: SendFileRequest, userEmail: String): Application {
        val fileContent =
            convertFileContentToListOfString(logRequest.file.inputStream.readBytes().toString(Charsets.UTF_8))
        val user = userService.findUserByEmail(FindUserByEmailQuery(userEmail))
        val application = applicationStorageService.findApplicationByUserAndName(user, logRequest.applicationName)
        if (application.isPresent) {
            forwardLogs(
                LogCommand(
                    userEmail = userEmail,
                    applicationId = application.get().id,
                    tag = "",
                    logFormat = LogFileTypes.UNKNOWN_FORMAT,
                    logs = fileContent
                )
            )
        } else {
            applicationStorageService.createApplicationWithCallback(
                CreateApplicationCommand(
                    logRequest.applicationName,
                    user
                )
            ) {
                forwardLogs(
                    LogCommand(
                        userEmail = userEmail,
                        applicationId = application.get().id,
                        tag = "",
                        logFormat = LogFileTypes.UNKNOWN_FORMAT,
                        logs = fileContent
                    )
                )
            }
        }
        return application.get()
    }

    private fun convertFileContentToListOfString(
        fileContent: String
    ): List<String> {
        val logMessages = mutableListOf<String>()
        var buffer = ""
        fileContent.lines().filter { it.isNotEmpty() }.forEach {
            buffer = if (it.first().isWhitespace()) {
                buffer.plus(it.trim().plus(" "))
            } else {
                if (buffer.isNotEmpty())
                    logMessages.add(buffer.trim())
                it.trim().plus(" ")
            }
        }
        if (buffer.isNotEmpty()) {
            logMessages.add(buffer.trim())
        }
        return logMessages
    }

    override fun uploadSampleData(userEmail: String) {
        val applicationNames = listOf("hdfs_node", "node_manager", "resource_manager", "name_node")
        val user = userService.findUserByEmail(FindUserByEmailQuery(userEmail))
        fun uploadSampleData(application: Application) {
            val fileContentAsListOfString = convertFileContentToListOfString(
                File("${resourcesPath}sample_data/${application.name}")
                    .inputStream()
                    .readBytes()
                    .toString(Charsets.UTF_8)
            )
            forwardLogs(
                LogCommand(
                    userEmail = userEmail,
                    applicationId = application.id,
                    tag = "",
                    logFormat = LogFileTypes.UNKNOWN_FORMAT,
                    logs = fileContentAsListOfString
                )
            )
        }

        for (appName in applicationNames) {
            try {
                val appOld = applicationStorageService.findApplicationByUserAndName(user, appName)
                if (appOld.isPresent) {
                    applicationStorageService.deleteApplicationWithCallback(DeleteApplicationCommand(appOld.get().id)) {
                        applicationStorageService.createApplicationWithCallback(
                            CreateApplicationCommand(
                                appName,
                                user
                            )
                        ) {
                            uploadSampleData(it)
                        }
                    }
                } else {
                    applicationStorageService.createApplicationWithCallback(CreateApplicationCommand(appName, user)) {
                        uploadSampleData(it)
                    }
                }
            } catch (e: Exception) {
                logger.error("Error while creating sample app $appName", e)
            }
        }
    }
}
