package ai.logsight.backend

import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.extensions.toApplication
import ai.logsight.backend.application.ports.out.persistence.ApplicationEntity
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTO
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTOActions
import ai.logsight.backend.application.utils.NameParser
import ai.logsight.backend.users.extensions.toUser
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import ai.logsight.backend.users.ports.out.persistence.UserType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

object TestInputConfig {

    val passwordEncoder = BCryptPasswordEncoder()

    const val baseEmail = "testemail@gmail.com"
    const val basePassword = "testpassword"
    const val baseAppName = "test_app"

    val baseUserEntity = UserEntity(
        email = baseEmail,
        password = passwordEncoder.encode(basePassword),
        userType = UserType.ONLINE_USER,
        activated = true
    )
    val baseUser = baseUserEntity.toUser()

    val baseAppEntity = ApplicationEntity(
        name = baseAppName,
        applicationKey = NameParser().slugify(baseAppName),
        user = baseUserEntity,
        status = ApplicationStatus.READY
    )

    val baseApp = baseAppEntity.toApplication()

    val baseAppDtoCreate =
        ApplicationDTO(
            baseUser.id,
            baseApp.name,
            baseApp.applicationKey,
            baseUser.key,
            action = ApplicationDTOActions.CREATE
        )
    val baseAppDtoDelete =
        ApplicationDTO(
            baseUser.id,
            baseApp.name,
            baseApp.applicationKey,
            baseUser.key,
            action = ApplicationDTOActions.DELETE
        )
}
