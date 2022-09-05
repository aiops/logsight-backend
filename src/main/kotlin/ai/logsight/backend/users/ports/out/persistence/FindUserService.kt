package ai.logsight.backend.users.ports.out.persistence

import ai.logsight.backend.users.domain.User
import java.util.*

interface FindUserService {
    fun findUserById(userId: UUID): User
    fun findUserByEmail(email: String): User
    fun findUserByStripeId(stripeId: String): User
}