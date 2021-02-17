package com.loxbear.logsight.entities

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "users")
data class LogsightUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "email")
    val email: String,

    @Column(name = "password")
    val password: String,

    @Column(name = "key")
    val key: String,

    @Column(name = "date_created")
    val dateCreated: LocalDateTime = LocalDateTime.now()
)