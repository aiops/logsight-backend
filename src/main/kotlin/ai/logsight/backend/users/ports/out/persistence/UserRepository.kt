package ai.logsight.backend.users.ports.out.persistence

import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {
    @Cacheable(value = ["users"], key = "#email")
    fun findByEmail(email: String): UserEntity?

    @Override
    @CacheEvict(cacheNames = ["users"], beforeInvocation = false, key = "#result.email")
    override fun <S : UserEntity> save(entity: S): S

    @Override
    @CacheEvict(cacheNames = ["users"], beforeInvocation = false, key = "#result.email")
    override fun deleteById(id: UUID)

    @Override
    fun findUserByStripeId(stripeId: String): UserEntity?
}
