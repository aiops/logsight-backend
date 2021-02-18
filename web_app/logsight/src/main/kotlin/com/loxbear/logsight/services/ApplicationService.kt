package com.loxbear.logsight.services

import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.LoginUserForm
import com.loxbear.logsight.models.RegisterUserForm
import com.loxbear.logsight.repositories.ApplicationRepository
import com.loxbear.logsight.repositories.UserRepository
import org.springframework.stereotype.Service
import utils.KeyGenerator

@Service
class ApplicationService(val repository: ApplicationRepository) {

}