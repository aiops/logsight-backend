package com.loxbear.logsight.repositories

import com.loxbear.logsight.entities.LogsightUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<LogsightUser, Long> {

}