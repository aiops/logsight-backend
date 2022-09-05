package ai.logsight.backend.limiter

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class RateLimitController(
    val userDetailsService: UserDetailsService
) {

    @GetMapping("/api/v1/test")
    @ResponseStatus(HttpStatus.OK)
    fun getName(authentication: Authentication): String {
        val user = userDetailsService.loadUserByUsername(authentication.name)
        return "Hello $user"
    }
}
