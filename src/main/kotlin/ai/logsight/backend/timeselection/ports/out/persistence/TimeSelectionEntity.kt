package ai.logsight.backend.timeselection.ports.out.persistence

import ai.logsight.backend.users.ports.out.persistence.UserEntity
import javax.persistence.*

@Entity
@Table(name = "time_selection")
data class TimeSelectionEntity(
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
    @JoinColumn(name = "id")
    val user: UserEntity
)
