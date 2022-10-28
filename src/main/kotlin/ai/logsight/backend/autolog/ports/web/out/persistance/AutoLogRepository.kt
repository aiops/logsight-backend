package ai.logsight.backend.autolog.ports.web.out.persistance

import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AutoLogRepository : JpaRepository<AutoLogEntity, UUID> {

}
