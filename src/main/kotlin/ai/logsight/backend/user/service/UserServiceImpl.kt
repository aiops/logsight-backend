package ai.logsight.backend.user.service

import ai.logsight.backend.email.service.EmailService
import ai.logsight.backend.exceptions.InvalidTokenException
import ai.logsight.backend.exceptions.PasswordsNotMatchException
import ai.logsight.backend.token.service.TokenService
import ai.logsight.backend.user.domain.User
import ai.logsight.backend.user.persistence.UserStorageService
import ai.logsight.backend.user.service.command.* // ktlint-disable no-wildcard-imports // ktlint-disable no-unused-imports
import ai.logsight.backend.user.service.command.ActivateUserCommand
import org.springframework.stereotype.Service

@Service
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

    /**
     * Activate the user given the activation link.
     */
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

    /**
     * Validate the token and change the user password.
     */
    override fun resetPasswordWithToken(resetPasswordCommand: ResetPasswordCommand): User {
        // check if token exists in DB
        val passwordResetToken = tokenService.findTokenById(resetPasswordCommand.passwordResetToken)
        // Check if matches user and not expired
        val validToken = tokenService.checkActivationToken(passwordResetToken)
        if (!validToken) throw InvalidTokenException()

        // Validate password
        if (resetPasswordCommand.password == resetPasswordCommand.repeatPassword) {
            val user = userStorageService.findUserByEmail(resetPasswordCommand.email)
            user.password = resetPasswordCommand.password
            return userStorageService.saveUser(user)
        }
        throw PasswordsNotMatchException()
    }

    /**
     * Create a password reset token and email the user.
     */
    override fun generateForgotPasswordTokenAndSendEmail(createTokenCommand: CreateTokenCommand) {
        val user = userStorageService.findUserByEmail(createTokenCommand.email)
        val passwordResetToken = tokenService.createPasswordResetToken(user.id)
        emailService.sendPasswordResetEmail(passwordResetToken, user)
    }
}
