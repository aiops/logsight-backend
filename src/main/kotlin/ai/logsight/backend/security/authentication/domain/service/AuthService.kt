package ai.logsight.backend.security.authentication.domain.service

import ai.logsight.backend.security.authentication.domain.AuthenticationToken

interface AuthService {
    fun authenticateUser(username: String, password: String): AuthenticationToken
}
