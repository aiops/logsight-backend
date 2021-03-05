package com.loxbear.logsight.repositories

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.entities.enums.ApplicationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ApplicationRepository : JpaRepository<Application, Long> {
    fun findAllByUser(user: LogsightUser): List<Application>

    @Modifying
    @Query("""update Application a set a.status = :status where a.id = :applicationId""")
    fun updateApplicationStatus(applicationId: Long, status: ApplicationStatus)
}