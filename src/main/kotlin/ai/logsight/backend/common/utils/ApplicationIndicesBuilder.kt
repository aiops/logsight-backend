package ai.logsight.backend.common.utils

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.users.domain.User
import org.springframework.stereotype.Service

@Service
class ApplicationIndicesBuilder(val applicationStorageService: ApplicationStorageService) {
    fun buildIndices(user: User, application: Application?, indexType: String) =
        applicationStorageService.findAllApplicationsByUser(user)
            .filter {
                application?.let { application -> application.id == it.id } ?: true
            }
            .joinToString(",") {
                "${
                user.key.lowercase()
                    .filter { it2 -> it2.isLetterOrDigit() }
                }_${it.name}_$indexType"
            }
}
