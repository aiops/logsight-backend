// package ai.logsight.backend.security.authentication
//
// import ai.logsight.backend.security.SecurityConstants
// import ai.logsight.backend.security.authentication.domain.AuthenticationToken
// import ai.logsight.backend.user.service.command.CreateLoginCommand
// import com.auth0.jwt.JWT
// import com.auth0.jwt.algorithms.Algorithm
// import org.springframework.security.authentication.AuthenticationManager
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
// import org.springframework.security.core.context.SecurityContextHolder
// import org.springframework.stereotype.Service
// import java.util.*
//
// @Service
// class AuthServiceImpl(private val authenticationManager: AuthenticationManager) : AuthService {
//
//    override fun authenticateUser(createLoginCommand: CreateLoginCommand): AuthenticationToken {
//        return with(
//            authenticationManager.authenticate(
//                UsernamePasswordAuthenticationToken(createLoginCommand.email, createLoginCommand.password)
//            )
//        ) {
//            SecurityContextHolder.getContext().authentication = this
//            AuthenticationToken(
//                JWT.create()
//                    .withSubject(this.name)
//                    .withExpiresAt(Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
//                    .sign(Algorithm.HMAC512(SecurityConstants.SECRET.toByteArray()))
//            )
//        }
//    }
// }
