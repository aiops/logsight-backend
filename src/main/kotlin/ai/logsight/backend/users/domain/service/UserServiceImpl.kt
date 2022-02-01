package ai.logsight.backend.users.domain.service

import ai.logsight.backend.email.domain.EmailContext
import ai.logsight.backend.email.domain.service.EmailServiceImpl
import ai.logsight.backend.exceptions.InvalidTokenException
import ai.logsight.backend.exceptions.PasswordsNotMatchException
import ai.logsight.backend.token.service.TokenService
import ai.logsight.backend.users.domain.LocalUser
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.domain.service.command.*
import ai.logsight.backend.users.domain.service.query.FindUserByEmailQuery
import ai.logsight.backend.users.ports.out.external.ExternalServiceManager
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userStorageService: UserStorageService,
    private val tokenService: TokenService,
    private val emailService: EmailServiceImpl,
    private val externalServices: ExternalServiceManager
) : UserService {
    override fun createUser(createUserCommand: CreateUserCommand): User {
        // create user
        val savedUser = userStorageService.createUser(createUserCommand.email, createUserCommand.password)

        // send Activation email
        sendActivationEmail(SendActivationEmailCommand(savedUser.email))
        // initialize external services
        externalServices.initializeServicesForUser(savedUser)
        // return user domain object
        return savedUser
    }

    override fun sendActivationEmail(sendActivationEmailCommand: SendActivationEmailCommand) {
        val user = userStorageService.findUserByEmail(sendActivationEmailCommand.email)
        // generate token
        val activationToken = tokenService.createActivationToken(user.id)
        // generate user activation URL
        val emailContext = EmailContext(
            userEmail = user.email, token = activationToken, title = "Activate your account"
        )
        // send email
//        emailService.sendActivationEmail(emailContext)
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
        return userStorageService.activateUser(activateUserCommand.email)
    }

    override fun changePassword(changePasswordCommand: ChangePasswordCommand): User {
        return userStorageService.changePassword(
            changePasswordCommand.email, changePasswordCommand.newPassword, changePasswordCommand.confirmNewPassword
        )
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

    override fun findUserByEmail(findUserByEmailQuery: FindUserByEmailQuery): User {
        return userStorageService.findUserByEmail(findUserByEmailQuery.email)
    }

    override fun createLocalUser(createUserCommand: CreateUserCommand): LocalUser {
        return userStorageService.createLocalUser(createUserCommand.email, createUserCommand.password)
    }

    /**
     * Create a password reset token and email the user.
     */
    override fun generateForgotPasswordTokenAndSendEmail(createTokenCommand: CreateTokenCommand) {
        val user = userStorageService.findUserByEmail(createTokenCommand.email)
        val passwordResetToken = tokenService.createPasswordResetToken(user.id)
        val emailContext = EmailContext(
            userEmail = user.email, token = passwordResetToken, title = "Reset password | logsight.ai"
        )
        emailService.sendPasswordResetEmail(emailContext)
    }
}
