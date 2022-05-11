package ai.logsight.backend.security.authentication.domain.service

import ai.logsight.backend.security.SecurityConstants
import ai.logsight.backend.security.authentication.domain.AuthenticationToken
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthServiceImpl(private val authenticationManager: AuthenticationManager) : AuthService {

    override fun authenticateUser(username: String, password: String): AuthenticationToken {
        return with(
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(username, password)
            )
        ) {
            SecurityContextHolder.getContext().authentication = this
            AuthenticationToken(
                JWT.create().withSubject(this.name)
                    .withExpiresAt(Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                    .sign(Algorithm.HMAC512(SecurityConstants.SECRET.toByteArray()))
            )
        }
    }
}
