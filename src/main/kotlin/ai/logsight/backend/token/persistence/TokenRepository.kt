package ai.logsight.backend.token.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TokenRepository : JpaRepository<TokenEntity, UUID>
