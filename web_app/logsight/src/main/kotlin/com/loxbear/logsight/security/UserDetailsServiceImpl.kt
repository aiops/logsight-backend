package com.loxbear.logsight.security

import com.loxbear.logsight.repositories.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UsernameNotFoundException

import org.springframework.security.core.userdetails.UserDetails

import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import kotlin.jvm.Throws


@Service
class UserDetailsServiceImpl(val applicationUserRepository: UserRepository) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val logsightUser = applicationUserRepository.findByEmail(username).orElseThrow { UsernameNotFoundException(username) }
        return User(logsightUser.email, logsightUser.password, emptyList())
    }
}