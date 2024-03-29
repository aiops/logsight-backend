package ai.logsight.backend.users.domain.service

import ai.logsight.backend.email.domain.EmailContext
import ai.logsight.backend.email.domain.service.EmailService
import ai.logsight.backend.email.domain.service.helpers.EmailTemplateTypes
import ai.logsight.backend.timeselection.domain.service.TimeSelectionService
import ai.logsight.backend.token.service.TokenService
import ai.logsight.backend.users.domain.OnlineUser
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.domain.service.command.*
import ai.logsight.backend.users.domain.service.query.FindUserQuery
import ai.logsight.backend.users.exceptions.PasswordsNotMatchException
import ai.logsight.backend.users.exceptions.UserAlreadyActivatedException
import ai.logsight.backend.users.exceptions.UserExistsException
import ai.logsight.backend.users.exceptions.UserNotActivatedException
import ai.logsight.backend.users.ports.out.external.ExternalServiceManager
import ai.logsight.backend.users.ports.out.external.exceptions.ExternalServiceException
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.mail.MailException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userStorageService: UserStorageService,
    private val tokenService: TokenService,
    private val emailService: EmailService,
    private val externalServices: ExternalServiceManager,
    private val timeSelectionService: TimeSelectionService,
    private val passwordEncoder: PasswordEncoder
) : UserService {

    val logger: Logger = LoggerFactory.getLogger(UserServiceImpl::class.java)

    override fun createOnlineUser(createUserCommand: CreateUserCommand): OnlineUser {
        logger.info("Creating online user.")
        if (userStorageService.checkEmailExists(createUserCommand.email)) {
            val user = userStorageService.findUserByEmail(createUserCommand.email)
            if (user.activated) throw UserExistsException() else (throw UserNotActivatedException())
        }
        // create user
        val savedUser = userStorageService.createOnlineUser(createUserCommand.email, createUserCommand.password)
        // send Activation email
        try {
            logger.info("Sending activation email to user ${savedUser.email}", this::createOnlineUser.name)
            sendActivationEmail(SendActivationEmailCommand(savedUser.email))
            logger.info("Activation email successfully sent to user ${savedUser.email}", this::createOnlineUser.name)
        } catch (e: MailException) {
            logger.error("Mail was not sent. Communication to mail client failed. Try again.")
        }

        // return user domain object
        return savedUser
    }

    override fun sendActivationEmail(sendActivationEmailCommand: SendActivationEmailCommand) {
        val user = userStorageService.findUserByEmail(sendActivationEmailCommand.email)
        if (user.activated) {
            throw UserAlreadyActivatedException()
        }
        // generate token
        val activationToken = tokenService.createActivationToken(user.id)
        // generate user activation URL
        // TODO: 01.02.22 Move title of emailContext to config or templates. Should not be here.
        val emailContext = EmailContext(
            userEmail = user.email,
            token = activationToken,
            title = "Activate your account",
            template = sendActivationEmailCommand.template
        )
        // send email
        emailService.sendActivationEmail(emailContext)
    }

    override fun deleteUser(deleteUserCommand: DeleteUserCommand) {
        val deletedUser = userStorageService.deleteUser(deleteUserCommand.userId)
        externalServices.teardownServicesForUser(deletedUser)
    }

    /**
     * Activate the user given the activation link.
     */
    override fun activateUser(activateUserCommand: ActivateUserCommand): User {
        logger.info("Creating offline user.")
        val user = userStorageService.findUserById(activateUserCommand.id)
        if (user.activated) {
            return user
        }
        val activationToken = tokenService.findTokenById(activateUserCommand.activationToken)
        // check activation token
        tokenService.checkActivationToken(activationToken)

        // initialize external services
        externalServices.initializeServicesForUser(user) // this will throw an error

        // setup predefined timestamps
        timeSelectionService.createPredefinedTimeSelections(user)
        // activate user
        return userStorageService.activateUser(user.email)
    }

    override fun changePassword(changePasswordCommand: ChangePasswordCommand): User {
        val user = userStorageService.findUserByEmail(changePasswordCommand.email)
        if (!user.activated) {
            throw UserNotActivatedException()
        }

        if (!passwordEncoder.matches(changePasswordCommand.oldPassword, user.password)) {
            throw PasswordsNotMatchException("Invalid password. Please retype your old password correctly.")
        }

        return userStorageService.changePassword(user.id, changePasswordCommand.newPassword)
    }

    /**
     * Validate the token and change the user password.
     */
    override fun resetPasswordWithToken(resetPasswordCommand: ResetPasswordCommand): User {
        val user = userStorageService.findUserById(resetPasswordCommand.id)
        if (!user.activated) {
            throw UserNotActivatedException()
        }
        // check if token exists in DB
        val passwordResetToken = tokenService.findTokenById(resetPasswordCommand.passwordResetToken)
        // Check if matches user and not expired
        tokenService.checkPasswordResetToken(passwordResetToken)

        return userStorageService.changePassword(
            resetPasswordCommand.id, resetPasswordCommand.password
        )
    }

    override fun findUser(findUserQuery: FindUserQuery): User {
        return userStorageService.findUserById(findUserQuery.userId)
    }

    override fun createUser(createUserCommand: CreateUserCommand): User {
        if (userStorageService.checkEmailExists(createUserCommand.email)) {
            val user = userStorageService.findUserByEmail(createUserCommand.email)
            if (user.activated) throw UserExistsException() else (throw UserNotActivatedException())
        }
        val user = userStorageService.createUser(createUserCommand.email, createUserCommand.password)
        try {
            externalServices.initializeServicesForUser(user)
        } catch (e: ExternalServiceException) {
            userStorageService.deleteUser(user.id)
            throw ExternalServiceException(e.message)
        }
        // setup predefined timestamps
        timeSelectionService.createPredefinedTimeSelections(user)
        return user
    }

    /**
     * Create a password reset token and email the user.
     */
    override fun generateForgotPasswordTokenAndSendEmail(createTokenCommand: CreateTokenCommand) {
        val user = userStorageService.findUserByEmail(createTokenCommand.email)
        if (!user.activated) {
            throw UserNotActivatedException()
        }
        val passwordResetToken = tokenService.createPasswordResetToken(user.id)
        // TODO: 01.02.22 Move title of emailContext to config or templates. Should not be here.
        val emailContext = EmailContext(
            userEmail = user.email,
            token = passwordResetToken,
            title = "Reset your password",
            template = EmailTemplateTypes.RESET_PASSWORD_EMAIL
        )
        logger.info(
            "Sending password reset email to user ${user.id}", this::generateForgotPasswordTokenAndSendEmail.name
        )
        emailService.sendPasswordResetEmail(emailContext)
    }
}
