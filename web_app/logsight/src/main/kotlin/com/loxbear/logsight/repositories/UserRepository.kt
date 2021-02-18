package com.loxbear.logsight.repositories

import com.loxbear.logsight.entities.LogsightUser
import org.apache.catalina.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<LogsightUser, Long> {
    fun findByEmail(email: String): Optional<LogsightUser>

    fun findByEmailAndPassword(email: String, password: String): Optional<LogsightUser>

}