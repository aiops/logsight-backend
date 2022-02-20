package ai.logsight.backend.users.ports.out.external

import ai.logsight.backend.users.domain.User

class ExternalPayment : ExternalService {
    override fun initialize(user: User) {
        println("Initialized ExternalPayment")
    }
}
