package ai.logsight.backend.security

import ai.logsight.backend.users.extensions.toUser
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(val applicationUserRepository: UserRepository) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val logsightUser =
            applicationUserRepository.findByEmail(username) ?: throw UsernameNotFoundException(username)
        if (logsightUser.activated) return User(logsightUser.email, logsightUser.password, emptyList())
        else throw UsernameNotFoundException(username)
    }

    @Throws(UsernameNotFoundException::class)
    fun loadUser(username: String): ai.logsight.backend.users.domain.User {
        val logsightUser =
            applicationUserRepository.findByEmail(username) ?: throw UsernameNotFoundException(username)
        if (logsightUser.activated) return logsightUser.toUser()
        else throw UsernameNotFoundException(username)
    }
}
