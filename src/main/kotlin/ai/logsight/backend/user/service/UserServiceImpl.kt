package ai.logsight.backend.user.service

import ai.logsight.backend.email.domain.EmailContext
import ai.logsight.backend.email.service.EmailServiceImpl
import ai.logsight.backend.exceptions.InvalidTokenException
import ai.logsight.backend.exceptions.PasswordsNotMatchException
import ai.logsight.backend.token.service.TokenService
import ai.logsight.backend.user.adapters.persistence.UserStorageService
import ai.logsight.backend.user.domain.LocalUser
import ai.logsight.backend.user.domain.User
import ai.logsight.backend.user.service.command.ActivateUserCommand
import ai.logsight.backend.user.service.command.ChangePasswordCommand
import ai.logsight.backend.user.service.command.CreateTokenCommand
import ai.logsight.backend.user.service.command.CreateUserCommand
import ai.logsight.backend.user.service.command.ResetPasswordCommand
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userStorageService: UserStorageService,
    private val tokenService: TokenService,
    private val emailService: EmailServiceImpl,
) : UserService {
    override fun createUser(createUserCommand: CreateUserCommand): User {
        // create user
        val savedUser = userStorageService.createUser(createUserCommand)
        // generate token
        val activationToken = tokenService.createActivationToken(savedUser.id)
        // generate user activation URL
        val emailContext = EmailContext(userEmail = savedUser.email, token = activationToken)
        // send email
        emailService.sendActivationEmail(emailContext)
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

    override fun createLocalUser(createUserCommand: CreateUserCommand): LocalUser {
        return userStorageService.createLocalUser(createUserCommand)
    }

    /**
     * Create a password reset token and email the user.
     */
    override fun generateForgotPasswordTokenAndSendEmail(createTokenCommand: CreateTokenCommand) {
        val user = userStorageService.findUserByEmail(createTokenCommand.email)
        val passwordResetToken = tokenService.createPasswordResetToken(user.id)
        val emailContext = EmailContext(
            userEmail = user.email,
            token = passwordResetToken,
        )
        emailService.sendPasswordResetEmail(emailContext)
    }
}
