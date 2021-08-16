package com.loxbear.logsight.repositories

import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.entities.PredefinedTime
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PredefinedTimesRepository : JpaRepository<PredefinedTime, Long> {
    fun findAllByUser(user: LogsightUser): List<PredefinedTime>
}