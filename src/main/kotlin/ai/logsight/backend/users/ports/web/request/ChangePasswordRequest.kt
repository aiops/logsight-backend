package ai.logsight.backend.users.ports.web.request

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class ChangePasswordRequest(
    @get:NotNull(message = "id must not be empty.")
    @get:Pattern(
        regexp = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",
        message = "id must be UUID type."
    )
    val userId: String,
    @get:NotEmpty(message = "oldPassword must not be empty.")
    @get:Size(
        min = 8,
        message = "oldPassword must be at least 8 characters."
    ) val oldPassword: String,
    @get:NotEmpty(message = "newPassword must not be empty.")
    @get:Size(
        min = 8,
        message = "newPassword must be at least 8 characters."
    ) val newPassword: String,

)
