package ai.logsight.backend.security.authentication

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.security.authentication.domain.service.AuthService
import ai.logsight.backend.security.authentication.response.LoginResponse
import ai.logsight.backend.security.authentication.response.UserDTO
import ai.logsight.backend.users.exceptions.UserNotActivatedException
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Api(tags = ["Authentication"], description = " ")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UserStorageService
) {

    private val logger = LoggerImpl(AuthController::class.java)

    /**
     * login user (authenticate)
     */
    @ApiOperation("Login user")
    @PostMapping("/login")
    fun login(@Valid @RequestBody loginUserRequest: LoginUserRequest): LoginResponse {
        logger.info("Login initiated for user ${loginUserRequest.email}.")
        val user = userService.findUserByEmail(loginUserRequest.email)
        if (!user.activated) throw UserNotActivatedException()
        val token = authService.authenticateUser(
            username = loginUserRequest.email, password = loginUserRequest.password
        )
        logger.info("Login token sent back as a response for user ${loginUserRequest.email}.")
        return LoginResponse(token = token.token, user = UserDTO(user.id, user.email))
    }
}
