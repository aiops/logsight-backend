import com.loxbear.logsight.entities.LogsightUser
import java.util.HashSet

import org.springframework.security.core.userdetails.UsernameNotFoundException

import org.springframework.security.core.authority.SimpleGrantedAuthority

import org.springframework.security.core.GrantedAuthority

import org.springframework.security.core.userdetails.UserDetails

import org.springframework.security.crypto.password.PasswordEncoder

import com.loxbear.logsight.repositories.UserRepository
import org.springframework.security.core.userdetails.User

import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service


@Service
class UserService(
        val repository: UserRepository,
) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(email: String): UserDetails {
        val logsightUser = repository.findByEmail(email)
                .orElseThrow { UsernameNotFoundException(String.format("User with email [%d] does not exists", email)) }
        return User(logsightUser.email, logsightUser.password, listOf())
    }
}