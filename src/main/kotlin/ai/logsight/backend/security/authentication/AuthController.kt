package ai.logsight.backend.security.authentication

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.security.authentication.domain.service.AuthService
import ai.logsight.backend.security.authentication.response.GetUserResponse
import ai.logsight.backend.security.authentication.response.LoginResponse
import ai.logsight.backend.security.authentication.response.UserDTO
import ai.logsight.backend.users.exceptions.UserNotActivatedException
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Api(tags = ["Authentication"], description = "User authentication")
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
    @ResponseStatus(HttpStatus.OK)
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

    @ApiOperation("Get authenticated user")
    @GetMapping("/user")
    @ResponseStatus(HttpStatus.OK)
    fun getUser(authentication: Authentication): GetUserResponse {
        logger.info("Getting information for the already authenticated user.")
        val user = userService.findUserByEmail(authentication.name)
        logger.info("User found in database. Sending response.", this::getUser.name)
        return GetUserResponse(user.id, user.email)
    }
}
