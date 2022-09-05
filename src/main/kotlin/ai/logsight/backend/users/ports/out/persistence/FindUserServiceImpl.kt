package ai.logsight.backend.users.ports.out.persistence

import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.exceptions.UserNotFoundException
import ai.logsight.backend.users.extensions.toUser
import org.springframework.stereotype.Service
import java.util.*

@Service
class FindUserServiceImpl(
    private val userRepository: UserRepository
) : FindUserService {
    override fun findUserById(userId: UUID): User = userRepository.findById(userId)
        .orElseThrow { UserNotFoundException("User with id $userId not found.") }
        .toUser()

    override fun findUserByEmail(email: String): User = userRepository.findByEmail(email)
        ?.toUser() ?: throw UserNotFoundException("User with email $email not found.")

    override fun findUserByStripeId(stripeId: String):  User = userRepository.findUserByStripeId(stripeId)
        ?.toUser() ?: throw UserNotFoundException("User with stripeId $stripeId not found.")
}
