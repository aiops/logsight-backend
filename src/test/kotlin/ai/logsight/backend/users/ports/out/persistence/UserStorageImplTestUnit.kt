package ai.logsight.backend.users.ports.out.persistence

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

internal class UserStorageImplTestUnit {
    private val userRepository: UserRepository = mockk()
    private val passwordEncoder = BCryptPasswordEncoder()
    val service = UserStorageImpl(userRepository, passwordEncoder)
}
