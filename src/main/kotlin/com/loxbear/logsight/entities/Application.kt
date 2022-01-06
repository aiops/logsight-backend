package com.loxbear.logsight.entities

import com.loxbear.logsight.entities.enums.ApplicationStatus
import javax.persistence.*


@Entity
@Table(name = "applications")
data class Application(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "name")
    val name: String,

    @Column(name = "input_topic_name")
    val inputTopicName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val status: ApplicationStatus,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: LogsightUser,
)