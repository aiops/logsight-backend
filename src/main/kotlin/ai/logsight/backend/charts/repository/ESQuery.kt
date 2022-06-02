package ai.logsight.backend.charts.repository

class ESQuery(
    var query: String = ""
) {
    fun modifyTime(startTime: String, stopTime: String): ESQuery {
        this.query = this.query.replace("start_time", startTime).replace("stop_time", stopTime)
        return this
    }

    fun modifyField(field: String): ESQuery {
        this.query = this.query.replace("field_value", field)
        return this
    }


}
