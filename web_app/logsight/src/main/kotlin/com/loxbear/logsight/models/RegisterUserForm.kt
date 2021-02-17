package com.loxbear.logsight.models

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "users")
data class RegisterUserForm(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "email")
    val email: String,

    @Column(name = "password")
    val password: String,

    @Column(name = "first_name")
    val firstName: String,

    @Column(name = "last_name")
    val lastName: String,

    @Column(name = "date_created")
    val dateCreated: LocalDateTime = LocalDateTime.now()
)