package ai.logsight.backend.user.service

import ai.logsight.backend.common.config.CommonConfigurationProperties
import ai.logsight.backend.email.service.EmailService
import ai.logsight.backend.email.service.dto.ActivateUserEmailDTO
import ai.logsight.backend.exceptions.InvalidTokenException
import ai.logsight.backend.exceptions.PasswordsNotMatchException
import ai.logsight.backend.token.service.TokenService
import ai.logsight.backend.user.domain.User
import ai.logsight.backend.user.persistence.UserStorageService
import ai.logsight.backend.user.service.command.ActivateUserCommand
import ai.logsight.backend.user.service.command.ChangePasswordCommand
import ai.logsight.backend.user.service.command.CreateTokenCommand
import ai.logsight.backend.user.service.command.CreateUserCommand
import ai.logsight.backend.user.service.command.ResetPasswordCommand
import org.springframework.stereotype.Service
import java.net.URI

@Service
class UserServiceImpl(
    private val commonConfig: CommonConfigurationProperties,
    private val userStorageService: UserStorageService,
    private val tokenService: TokenService,
    private val emailService: EmailService,
) : UserService {
    override fun createUser(createUserCommand: CreateUserCommand): User {
        // create user
        val savedUser = userStorageService.createUser(createUserCommand)
        // generate token
        val activationToken = tokenService.createActivationToken(savedUser.id)
        // generate user activation URL
        val activationURL = URI(
            commonConfig.baseURL.scheme,
            commonConfig.baseURL.authority,
            commonConfig.baseURL.path,
            "uuid=${savedUser.id}&token=$activationToken",
            commonConfig.baseURL.fragment
        ).resolve("/api/v1/user/activate").toURL() // TODO Can / should be this endpoint retrieved instead of hard-coded?
        // create activation email dto
        val activationEmailDTO = ActivateUserEmailDTO(savedUser.email, activationURL)
        // send email
        emailService.sendActivationEmail(activationEmailDTO)
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
        // emailService.sendPasswordResetEmail(passwordResetToken, user)
    }
}
