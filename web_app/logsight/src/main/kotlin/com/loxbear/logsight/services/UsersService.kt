package com.loxbear.logsight.services

import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.RegisterUserForm
import com.loxbear.logsight.repositories.UserRepository
import org.springframework.stereotype.Service
import utils.KeyGenerator

@Service
class UsersService(val repository: UserRepository) {

    fun createUser(form: RegisterUserForm): LogsightUser {
        return with(form) {
            if (repository.findByEmail(email).isPresent) {
                throw Exception("User with email $email already exists")
            }
            repository.save(LogsightUser(id = 0, email = email, password = password, key = KeyGenerator.generate()))
        }

    }
}