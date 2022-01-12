package ai.logsight.backend.user.persistence

import ai.logsight.backend.exceptions.EmailExistsException
import ai.logsight.backend.exceptions.PasswordsNotMatchException
import ai.logsight.backend.user.domain.User
import ai.logsight.backend.user.extensions.toUser
import ai.logsight.backend.user.extensions.toUserEntity
import ai.logsight.backend.user.service.UserNotFoundException
import ai.logsight.backend.user.service.command.ActivateUserCommand
import ai.logsight.backend.user.service.command.ChangePasswordCommand
import ai.logsight.backend.user.service.command.CreateUserCommand
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserStorageImpl(private val userRepository: UserRepository) : UserStorageService {
    override fun createUser(createUserCommand: CreateUserCommand): User {
        if (userRepository.findByEmail(createUserCommand.email).isPresent) throw EmailExistsException()

        val userEntity = UserEntity(email = createUserCommand.email, password = createUserCommand.password)
        val savedEntity = userRepository.save(userEntity)
        return savedEntity.toUser()
    }

    override fun activateUser(activateUserCommand: ActivateUserCommand): User {
        val userEntity = userRepository.findByEmail(activateUserCommand.email).orElseThrow { UserNotFoundException() }
        userEntity.activated = true
        return userRepository.save(userEntity).toUser()
    }

    override fun changePassword(changePasswordCommand: ChangePasswordCommand): User {
        val userEntity =
            userRepository.findByEmail(changePasswordCommand.email)
                .orElseThrow { UserNotFoundException() }

        if (changePasswordCommand.newPassword != changePasswordCommand.confirmNewPassword)
            throw PasswordsNotMatchException()

        // change password
        userEntity.password = changePasswordCommand.newPassword
        // save changes
        return userRepository.save(userEntity).toUser()
    }

    override fun findUserById(userId: UUID): User =
        userRepository.findById(userId).orElseThrow { UserNotFoundException() }.toUser()

    override fun findUserByEmail(email: String): User = userRepository
        .findByEmail(email)
        .orElseThrow { UserNotFoundException() }
        .toUser()

    override fun saveUser(user: User): User {
        return userRepository.save(user.toUserEntity()).toUser()
    }
}
