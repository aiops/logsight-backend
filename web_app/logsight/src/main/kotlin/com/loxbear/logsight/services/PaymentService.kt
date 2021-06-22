package com.loxbear.logsight.services

import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class PaymentService(val userRepository: UserRepository) {

    val logger = LoggerFactory.getLogger(PaymentService::class.java)

    @Transactional
    fun paymentSuccessful(user: LogsightUser, customerId: String?) {
        userRepository.updateCustomerIdAndHasPaid(customerId, user.id)
    }

    @Transactional
    fun updateHasPaid(user: LogsightUser, hasPaid: Boolean) {
        userRepository.updateHasPaid(hasPaid, user.id)
    }

    @Transactional
    fun createCustomerId(user: LogsightUser, customerId: String?) {
        userRepository.createCustomerId(customerId, user.id)
    }

}