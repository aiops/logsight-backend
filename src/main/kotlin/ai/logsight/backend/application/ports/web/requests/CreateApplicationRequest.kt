package ai.logsight.backend.application.ports.web.requests

import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.LowerCase
import java.util.*
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class CreateApplicationRequest(
    @Pattern(regexp = "^[A-Za-z0-9_]*$")
    val applicationName: String
)
