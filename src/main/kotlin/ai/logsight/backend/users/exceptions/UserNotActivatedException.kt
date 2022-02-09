package ai.logsight.backend.users.exceptions

class UserNotActivatedException(override val message: String? = "User is not activated. Please activate the user using the activation link sent to your email.") :
    RuntimeException(message)
