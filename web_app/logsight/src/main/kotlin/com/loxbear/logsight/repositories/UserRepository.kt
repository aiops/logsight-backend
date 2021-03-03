package com.loxbear.logsight.repositories

import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.UserModel
import org.apache.catalina.User
import org.joda.time.LocalDateTime
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<LogsightUser, Long> {
    fun findByEmail(email: String): Optional<LogsightUser>

    fun findByEmailAndPasswordAndActivatedIsTrue(email: String, password: String): Optional<LogsightUser>

    fun findByKey(key: String): Optional<LogsightUser>

    @Modifying
    @Query(
        """update LogsightUser u set u.activated = true where u.key = :key"""
    )
    fun activateUser(key: String)

}