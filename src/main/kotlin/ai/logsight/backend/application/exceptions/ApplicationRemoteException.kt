package ai.logsight.backend.application.exceptions

class ApplicationRemoteException(override val message: String? = null) : RuntimeException(message)
