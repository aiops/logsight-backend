package ai.logsight.backend.charts.repository

class ESQuery(
    var query: String = ""
) {
    fun modifyTime(startTime: String, stopTime: String, timeZone: String): ESQuery {
        this.query = this.query.replace("start_time", startTime).replace("stop_time", stopTime).replace("time_zone_parameter", timeZone)
        return this
    }
}
