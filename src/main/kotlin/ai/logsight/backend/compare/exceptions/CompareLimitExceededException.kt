package ai.logsight.backend.compare.exceptions

import javax.naming.LimitExceededException

class CompareLimitExceededException(override val message: String? = null) : LimitExceededException(message)
