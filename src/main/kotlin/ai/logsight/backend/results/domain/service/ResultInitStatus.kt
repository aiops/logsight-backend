package ai.logsight.backend.results.domain.service

import org.springframework.http.HttpStatus

enum class ResultInitStatus(private val mapping: Set<HttpStatus>) {
    PENDING(setOf()),
    DONE(setOf(HttpStatus.OK)),
    TIMED_OUT(setOf(HttpStatus.REQUEST_TIMEOUT)),
    FAILED(setOf());

    companion object {
        fun toResultInitStatus(httpStatus: HttpStatus): ResultInitStatus =
            when {
                DONE.mapping.contains(httpStatus) -> DONE
                TIMED_OUT.mapping.contains(httpStatus) -> TIMED_OUT
                else -> FAILED
            }
    }
}
