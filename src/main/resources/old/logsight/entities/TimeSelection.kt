package com.loxbear.logsight.entities

import com.loxbear.logsight.entities.enums.DateTimeType
import javax.persistence.*

@Entity
@Table(name = "time_selection")
data class TimeSelection(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "name")
    val name: String,

    @Column(name = "start_time")
    val startTime: String,

    @Column(name = "end_time")
    val endTime: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "date_time_type")
    val dateTimeType: DateTimeType,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: LogsightUser,
)