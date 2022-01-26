package ai.logsight.backend.logs.domain.service

import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.logs.domain.LogContext
import ai.logsight.backend.user.domain.service.UserService
import org.springframework.stereotype.Service

@Service
class LogsServiceImpl(
    userService: UserService,
    appService: ApplicationLifecycleService
) : LogsService {
    override fun forwardLogs(logContext: LogContext): Int {
        TODO("Not yet implemented")
    }
}
