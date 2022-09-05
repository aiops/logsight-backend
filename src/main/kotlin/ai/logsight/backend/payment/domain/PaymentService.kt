package ai.logsight.backend.payment.domain

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.domain.UserCategory
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class PaymentService(val userStorageService: UserStorageService) {

    val mapper = ObjectMapper().registerModule(KotlinModule())!!
    private val logger = LoggerImpl(PaymentService::class.java)


    @Transactional
    fun changeUserCategory(user: User, userCategory: UserCategory) {
        userStorageService.changeUserCategory(user.id, userCategory)
    }

//    @Transactional
//    fun createCustomerId(user: LogsightUser, customerId: String?) {
//        userRepository.createCustomerId(customerId, user.id)
//    }

}