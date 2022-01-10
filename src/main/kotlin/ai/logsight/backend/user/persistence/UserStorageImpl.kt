package ai.logsight.backend.user.persistence

import ai.logsight.backend.user.domain.User
import ai.logsight.backend.user.extensions.toUser
import ai.logsight.backend.user.service.UserNotFoundException
import ai.logsight.backend.user.service.command.ActivateUserCommand
import ai.logsight.backend.user.service.command.ChangePasswordCommand
import ai.logsight.backend.user.service.command.CreateUserCommand
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserStorageImpl(private val userRepository: UserRepository) : UserStorageService {
    override fun createUser(createUserCommand: CreateUserCommand): User {
        val userEntity = UserEntity(email = createUserCommand.email, password = createUserCommand.password)
        return userRepository.save(userEntity).toUser()
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
        // change password
        userEntity.password = changePasswordCommand.newPassword
        // save changes
        return userRepository.save(userEntity).toUser()
    }

    override fun getUserById(userId: UUID): User = userRepository.findById(userId).get().toUser()
}
