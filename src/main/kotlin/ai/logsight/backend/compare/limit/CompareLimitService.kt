package ai.logsight.backend.compare.limit

import ai.logsight.backend.compare.exceptions.CompareLimitExceededException
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.domain.UserCategory
import org.springframework.stereotype.Service

@Service
class CompareLimitService(
    val configProperties: CompareLimitConfigProperties
) {
    fun checkTagLimit(user: User, baselineTags: Map<String, String>, candidateTags: Map<String, String>): Boolean {
        val compareLimit = resolveCompareLimitFromUser(user.userCategory)
        if (baselineTags.size > compareLimit || candidateTags.size > compareLimit) {
            throw CompareLimitExceededException("Exceeded tag limit for user. Max tag limit allowed: $compareLimit")
        }
        return true
    }

    fun resolveCompareLimitFromUser(userType: UserCategory): Long {
        return when (userType) {
            UserCategory.FREEMIUM -> configProperties.freemium
            UserCategory.CORPORATE -> configProperties.corporate
            UserCategory.DEVELOPER -> configProperties.developer
        }
    }
}
