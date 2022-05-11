package ai.logsight.backend.flush.domain.service

import org.springframework.http.HttpStatus

enum class FlushStatus(private val mapping: Set<HttpStatus>) {
    PENDING(setOf()),
    DONE(setOf(HttpStatus.OK)),
    TIMED_OUT(setOf(HttpStatus.REQUEST_TIMEOUT)),
    FAILED(setOf());

    companion object {
        fun toFlushStatus(httpStatus: HttpStatus): FlushStatus =
            when {
                DONE.mapping.contains(httpStatus) -> DONE
                TIMED_OUT.mapping.contains(httpStatus) -> TIMED_OUT
                else -> FAILED
            }
    }
}
