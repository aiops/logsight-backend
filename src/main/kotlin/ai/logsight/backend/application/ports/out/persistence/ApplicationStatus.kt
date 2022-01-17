package ai.logsight.backend.application.ports.out.persistence

enum class ApplicationStatus {
    CREATING,
    READY,
    DELETING,
    DELETED,
}
