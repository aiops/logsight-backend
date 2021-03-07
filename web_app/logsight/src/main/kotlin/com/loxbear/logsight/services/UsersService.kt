package com.loxbear.logsight.services

import com.loxbear.logsight.encoder
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.RegisterUserForm
import com.loxbear.logsight.models.UserModel
import com.loxbear.logsight.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import utils.KeyGenerator

@Service
class UsersService(val repository: UserRepository,
                   val emailService: EmailService) {

    val logger = LoggerFactory.getLogger(UsersService::class.java)

    fun createUser(form: RegisterUserForm): LogsightUser {
        return with(form) {
            if (repository.findByEmail(email).isPresent) {
                throw Exception("User with email $email already exists")
            }
            repository.save(LogsightUser(id = 0, email = email, password = password, key = KeyGenerator.generate()))
        }

    }

    @Transactional
    fun registerUser(email: String): String? {
        return if (repository.findByEmail(email).isPresent) {
            return "User with email $email already exists"
        } else {
            val user = createUser(form = RegisterUserForm(email, encoder().encode("demo"), encoder().encode("demo")))
            emailService.sendActivationEmail(user)
            null
        }
    }

    fun findByKey(key: String): LogsightUser = repository.findByKey(key).orElseThrow { Exception("User with key $key not found") }

    @Transactional
    fun activateUser(key: String): UserModel {
        logger.info("Activating user with key [{}]", key)
        val user = findByKey(key)
        repository.activateUser(key)
        with(user) {
            return UserModel(id = id, email = email, activated = activated, key = key)
        }
    }

    fun findByEmail(email: String): LogsightUser {
        return repository.findByEmail(email).orElseThrow { Exception("User with email $email not found") }
    }
}