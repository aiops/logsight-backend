package com.loxbear.logsight.repositories

import com.loxbear.logsight.entities.Application
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationRepository : JpaRepository<Application, Long> {
}