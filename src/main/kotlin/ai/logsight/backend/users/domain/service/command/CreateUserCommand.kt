package ai.logsight.backend.users.domain.service.command

import javax.validation.constraints.Size

data class CreateUserCommand(
    val email: String,
    
    @get:Size(min = 8)
    val password: String,
)
