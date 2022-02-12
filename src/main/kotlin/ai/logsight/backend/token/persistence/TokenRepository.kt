package ai.logsight.backend.token.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TokenRepository : JpaRepository<TokenEntity, UUID> {
    fun findByUserId(id: UUID): List<TokenEntity?>
}
