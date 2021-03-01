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


    ) {
    override fun toString(): String {
        return "LogsightUser(id=$id, email='$email', password='$password', key='$key', dateCreated=$dateCreated, activationDate=$activationDate, activated=$activated)"
    }
}