package com.loxbear.logsight.entities

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "users")
data class LogsightUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "email", nullable = false)
    val email: String,

    @Column(name = "password", nullable = false)
    val password: String,

    @Column(name = "key", nullable = false)
    val key: String,

    @Column(name = "date_created", nullable = false)
    val dateCreated: LocalDateTime = LocalDateTime.now(),

    @Column(name = "activation_date", nullable = true)
    val activationDate: LocalDateTime? = null,

    @Column(name = "activated", nullable = false)
    val activated: Boolean = false,

    @Column(name = "has_paid", nullable = false)
    val hasPaid: Boolean = false,

    @Column(name = "stripe_customer_id")
    val stripeCustomerId: String? = null,

    @Column(name = "used_data")
    val usedData: Long = 0L,

    @Column(name = "available_data")
    val availableData: Long = 10000000L
) {
    override fun toString(): String {
        return "LogsightUser(id=$id, email=$email, availableData=$availableData, usedData=$usedData)"
    }
}