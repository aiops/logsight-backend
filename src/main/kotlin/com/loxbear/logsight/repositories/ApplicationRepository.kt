package com.loxbear.logsight.repositories

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.entities.enums.ApplicationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ApplicationRepository : JpaRepository<Application, Long> {
    fun findAllByUser(user: LogsightUser): List<Application>

    fun findByName(name: String): Optional<Application>

    @Modifying
    @Query("""update Application a set a.status = :status where a.id = :applicationId""")
    fun updateApplicationStatus(applicationId: Long, status: ApplicationStatus)

    @Modifying
    @Query("""update Application a set a.inputTopicName = :inputTopicName where a.id = :applicationId""")
    fun updateTopicName(applicationId: Long, inputTopicName: String)

}