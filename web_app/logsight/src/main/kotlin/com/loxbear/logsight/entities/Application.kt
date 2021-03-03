package com.loxbear.logsight.entities

import javax.persistence.*

@Entity
@Table(name = "applications")
data class Application(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "name")
    val name: String,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: LogsightUser,
)