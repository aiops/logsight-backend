package ai.logsight.backend.user.service

import ai.logsight.backend.email.service.EmailService
import ai.logsight.backend.exceptions.InvalidTokenException
import ai.logsight.backend.token.service.TokenService
import ai.logsight.backend.user.domain.User
import ai.logsight.backend.user.persistence.UserStorageService
import ai.logsight.backend.user.service.command.ActivateUserCommand
import ai.logsight.backend.user.service.command.ChangePasswordCommand
import ai.logsight.backend.user.service.command.CreateUserCommand
import ai.logsight.backend.user.service.command.ResetPasswordCommand
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Validated
class UserServiceImpl(
    private val userStorageService: UserStorageService,
    private val tokenService: TokenService,
    private val emailService: EmailService,
) : UserService {
    override fun createUser(createUserCommand: CreateUserCommand): User {
        // create user
        val savedUser = userStorageService.createUser(createUserCommand)
        // generate token
        val activationToken = tokenService.createActivationToken(savedUser.id)
        // send email with token
        emailService.sendActivationEmail(activationToken, savedUser)
        // return user domain object
        return savedUser
    }

    override fun activateUser(activateUserCommand: ActivateUserCommand): User {
        val activationToken = tokenService.findTokenById(activateUserCommand.activationToken)
        // check activation token
        val validToken = tokenService.checkActivationToken(activationToken)
        // check token validity
        if (!validToken) throw InvalidTokenException()
        // activate user
        return userStorageService.activateUser(activateUserCommand)
    }

    override fun changePassword(changePasswordCommand: ChangePasswordCommand): User {
        return userStorageService.changePassword(changePasswordCommand)
    }

    override fun resetPassword(resetPasswordCommand: ResetPasswordCommand): User {
        val passwordResetToken = tokenService.findTokenById(resetPasswordCommand.passwordResetToken)
        // check activation token
        val validToken = tokenService.checkActivationToken(passwordResetToken)
        // check token validity
        if (!validToken) throw InvalidTokenException()

        val user = userStorageService.getUserById(resetPasswordCommand.userId)
        // send email
        // TODO: 07.01.22 Implement email
        return user
    }
}
