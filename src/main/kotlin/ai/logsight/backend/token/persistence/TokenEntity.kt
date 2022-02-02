package ai.logsight.backend.token.persistence

import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class TokenEntity(
    val userId: UUID,
    val tokenType: TokenType,
    tokenDuration: Duration

) {
    @Id
    val token: UUID = UUID.randomUUID()
    val expiresAt: LocalDateTime = LocalDateTime.now().plus(tokenDuration)
}
