package ai.logsight.backend.security.authentication

import ai.logsight.backend.security.authentication.domain.AuthenticationToken
import ai.logsight.backend.user.service.command.CreateLoginCommand

interface AuthService {
    fun authenticateUser(createLoginCommand: CreateLoginCommand): AuthenticationToken
}
