package ai.logsight.backend.users.ports.out.persistence

import ai.logsight.backend.users.domain.OnlineUser
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.domain.UserCategory
import ai.logsight.backend.users.exceptions.EmailExistsException
import ai.logsight.backend.users.exceptions.UserNotFoundException
import ai.logsight.backend.users.extensions.toOnlineUser
import ai.logsight.backend.users.extensions.toUser
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class UserStorageImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UserStorageService {
    override fun createOnlineUser(email: String, password: String): OnlineUser {
        if (userRepository.findByEmail(email) != null) throw EmailExistsException("User with $email is already registered.")

        val userEntity = UserEntity(
            email = email,
            password = passwordEncoder.encode(password),
            userType = UserType.ONLINE_USER,
            userCategory = UserCategory.FREEMIUM

        )
        val savedEntity = userRepository.save(userEntity)
        return savedEntity.toOnlineUser()
    }

    override fun createUser(email: String, password: String): User {
        if (userRepository.findByEmail(email) != null) throw EmailExistsException("User with email $email is already registered.")
        val userEntity = UserEntity(
            email = email,
            password = passwordEncoder.encode(password),
            userType = UserType.LOCAL_USER,
            activated = true,
            activationDate = LocalDateTime.now(),
            hasPaid = true,
            userCategory = UserCategory.CORPORATE
        )
        return userRepository.save(userEntity)
            .toUser()
    }

    override fun checkEmailExists(email: String): Boolean {
        return userRepository.findByEmail(email) != null
    }

    override fun activateUser(email: String): User {
        val userEntity = userRepository.findByEmail(email)
            ?: throw UserNotFoundException("User with email $email doesn't exist in database.")
        userEntity.activated = true
        return userRepository.save(userEntity)
            .toUser()
    }

    override fun changePassword(id: UUID, newPassword: String): User {
        val userEntity = userRepository.findById(id)
            .orElseThrow { UserNotFoundException("User with id $id doesn't exist in database.") }

        // change password
        userEntity.password = passwordEncoder.encode(newPassword)
        // save changes
        return userRepository.save(userEntity)
            .toUser()
    }

    override fun findUserByStripeId(stripeId: String):  User = userRepository.findUserByStripeId(stripeId)
        ?.toUser() ?: throw UserNotFoundException("User with stripeId $stripeId not found.")

    override fun changeUserCategory(id: UUID, userCategory: UserCategory): User {
        val userEntity = userRepository.findById(id)
            .orElseThrow { UserNotFoundException("User with id $id doesn't exist in database.") }
        userEntity.userCategory = userCategory
        // save changes
        return userRepository.save(userEntity)
            .toUser()
    }

    override fun findUserById(userId: UUID): User = userRepository.findById(userId)
        .orElseThrow { UserNotFoundException("User with id $userId not found.") }
        .toUser()

    override fun findUserByEmail(email: String): User = userRepository.findByEmail(email)
        ?.toUser() ?: throw UserNotFoundException("User with email $email not found.")



    override fun deleteUser(id: UUID): User {
        val user = findUserById(id)
        userRepository.deleteById(user.id)
        return user
    }
}
