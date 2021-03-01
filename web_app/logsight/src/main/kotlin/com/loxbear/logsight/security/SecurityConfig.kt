package com.loxbear.logsight.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm.HMAC512
import com.fasterxml.jackson.databind.ObjectMapper
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.security.SecurityConstants.HEADER_STRING
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.io.IOException
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.jvm.Throws


class JWTAuthenticationFilter(val authenticationManager2: AuthenticationManager) : UsernamePasswordAuthenticationFilter() {
    @Throws(AuthenticationException::class)
    override fun attemptAuthentication(req: HttpServletRequest,
                                       res: HttpServletResponse): Authentication {
        return try {
            val creds: LogsightUser = ObjectMapper()
                .readValue(req.inputStream, LogsightUser::class.java)
            authenticationManager2.authenticate(
                UsernamePasswordAuthenticationToken(
                    creds.email,
                    creds.password,
                    ArrayList())
            )
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Throws(IOException::class, ServletException::class)
    override fun successfulAuthentication(req: HttpServletRequest,
                                          res: HttpServletResponse,
                                          chain: FilterChain,
                                          auth: Authentication) {
        val token: String = JWT.create()
            .withSubject((auth.principal as User).username)
            .withExpiresAt(Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
            .sign(HMAC512(SecurityConstants.SECRET.toByteArray()))
        res.addHeader(HEADER_STRING, SecurityConstants.TOKEN_PREFIX + token)
        res.addHeader("Access-Control-Expose-Headers", HEADER_STRING);
    }
}