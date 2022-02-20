package ai.logsight.backend.users.ports.out.external

import ai.logsight.backend.users.domain.User

interface ExternalService {
    fun initialize(user: User)
}
