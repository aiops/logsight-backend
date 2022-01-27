package ai.logsight.backend.security.authentication

import ai.logsight.backend.security.authentication.domain.service.AuthService
import ai.logsight.backend.security.authentication.response.LoginResponse
import ai.logsight.backend.security.authentication.response.UserDTO
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid

@Controller
class AuthController(
    private val authService: AuthService,
    private val userService: UserStorageService
) {

    /**
     * login user (authenticate)
     */

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginUserRequest: LoginUserRequest): LoginResponse {
        val token = authService.authenticateUser(
            username = loginUserRequest.email, password = loginUserRequest.password
        )
        val user = userService.findUserByEmail(loginUserRequest.email)
        return LoginResponse(token = token.token, user = UserDTO(user.id, user.email))
    }
}
