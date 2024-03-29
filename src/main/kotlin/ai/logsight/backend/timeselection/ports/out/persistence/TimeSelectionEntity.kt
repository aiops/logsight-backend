package ai.logsight.backend.timeselection.ports.out.persistence

import ai.logsight.backend.users.ports.out.persistence.UserEntity
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "time_selection")
class TimeSelectionEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name")
    val name: String,

    @Column(name = "start_time")
    val startTime: String,

    @Column(name = "end_time")
    val endTime: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "date_time_type")
    val dateTimeType: DateTimeType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserEntity
)
