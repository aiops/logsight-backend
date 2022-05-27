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

    fun modifyBaselineTags(baselineTags: String): ESQuery {
        this.query = this.query.replace("baselineTags_value", baselineTags)
        return this
    }
    fun modifyCompareId(compareId: String): ESQuery {
        if(compareId.isNotEmpty()){
            this.query = this.query.replace("compare_id", "{\"match_phrase\": {\"_id\": \"$compareId\"}}")
        }else{
            this.query = this.query.replace("compare_id", "")
        }
        return this
    }


}
