package com.loxbear.logsight.repositories

import com.loxbear.logsight.entities.LogsightUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<LogsightUser, Long> {
    fun findByEmail(email: String): Optional<LogsightUser>

    fun findByKey(key: String): Optional<LogsightUser>

    fun findByStripeCustomerId(key: String): Optional<LogsightUser>

    @Modifying
    @Query(
        """update LogsightUser u set u.stripeCustomerId = :customerId where u.id = :id"""
    )
    fun createCustomerId(customerId: String?, id: Long)

    @Modifying
    @Query(
        """update LogsightUser u set u.availableData = :availableData, u.hasPaid = true, u.stripeCustomerId = :customerId where u.id = :id"""
    )
    fun updateCustomerIdAndHasPaidandAndAvailableData(customerId: String?, id: Long, availableData: Long)

    @Modifying
    @Query(
        """update LogsightUser u set u.hasPaid = :hasPaid where u.id = :id"""
    )
    fun updateHasPaid(hasPaid: Boolean, id: Long)


    @Modifying
    @Query(
        """update LogsightUser u set u.availableData = :availableData where u.id = :id"""
    )
    fun updateAvailableData(availableData: Long, id: Long)

//    @Modifying
//    @Query(
//        """update LogsightUser u set u.limitApproaching = :limitApproaching where u.id = :id"""
//    )
//    fun updateLimitApproaching(limitApproaching: Boolean, id: Long)


    @Modifying
    @Query(
        """update LogsightUser u set u.usedData = :usedData where u.key = :key"""
    )
    fun updateUsedData(key: String, usedData: Long)

    fun existsByKey(key: String): Boolean

    fun existsByEmail(email: String): Boolean
}