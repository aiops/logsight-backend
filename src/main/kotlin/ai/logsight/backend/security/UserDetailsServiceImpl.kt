package ai.logsight.backend.security

import ai.logsight.backend.user.persistence.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import kotlin.jvm.Throws

@Service
class UserDetailsServiceImpl(val applicationUserRepository: UserRepository) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val logsightUser = applicationUserRepository.findByEmail(username).orElseThrow { UsernameNotFoundException(username) }
        if (logsightUser.activated)
            return User(logsightUser.email, logsightUser.password, emptyList())
        else throw UsernameNotFoundException(username)
    }
}
