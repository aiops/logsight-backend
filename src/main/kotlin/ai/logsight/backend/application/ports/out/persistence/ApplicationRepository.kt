package ai.logsight.backend.application.ports.out.persistence

import ai.logsight.backend.users.ports.out.persistence.UserEntity
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ApplicationRepository : JpaRepository<ApplicationEntity, UUID> {
    @Cacheable(cacheNames = ["applications_name"], key = "{#user, #applicationName}")
    fun findByUserAndName(user: UserEntity, applicationName: String): ApplicationEntity?
    fun findAllByUser(user: UserEntity): List<ApplicationEntity>

    @Override
    @Caching(
        evict = [
            CacheEvict(
                value = ["applications_name"],
                key = "{#entity.user, #entity.name}"
            ), CacheEvict(value = ["applications_id"], key = "#entity.id")
        ]
    )
    override fun <S : ApplicationEntity> save(entity: S): S
    @Override
    @Caching(
        evict = [
            CacheEvict(
                value = ["applications_name"],
                key = "{#entity.user, #entity.name}"
            ), CacheEvict(value = ["applications_id"], key = "#entity.id")
        ]
    )
    override fun delete(entity: ApplicationEntity)

    @Override
    @Cacheable(cacheNames = ["applications_id"], key = "#id")
    override fun findById(id: UUID): Optional<ApplicationEntity>
}
