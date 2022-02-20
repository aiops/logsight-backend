package ai.logsight.backend.compare.controller.response

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.JsonNode
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class CompareDataResponse(
    var applicationId: UUID? = null,
    var flushId: UUID? = null,
    var link: String? = "",
    @JsonAlias("risk")
    val risk: Long,
    @JsonAlias("total_n_log_messages")
    val totalLogCount: Long,
    @JsonAlias("count_baseline")
    val baselineLogCount: Long,
    @JsonAlias("count_candidate")
    val candidateLogCount: Long,
    @JsonAlias("candidate_perc")
    val candidateChangePercentage: Double,
    @JsonAlias("added_states")
    val addedStatesTotalCount: Long,
    @JsonAlias("added_states_info")
    val addedStatesReportPercentage: Double,
    @JsonAlias("added_states_fault")
    val addedStatesFaultPercentage: Double,
    @JsonAlias("deleted_states")
    val deletedStatesTotalCount: Long,
    @JsonAlias("deleted_states_info")
    val deletedStatesReportPercentage: Double,
    @JsonAlias("deleted_states_fault")
    val deletedStatesFaultPercentage: Double,
    @JsonAlias("recurring_states")
    val recurringStatesTotalCount: Long,
    @JsonAlias("recurring_states_info")
    val recurringStatesReportPercentage: Double,
    @JsonAlias("recurring_states_fault")
    val recurringStatesFaultPercentage: Double,
    @JsonAlias("frequency_change")
    val frequencyChangeTotalCount: Long,
    @JsonAlias("frequency_change_info")
    val frequencyChangeReportPercentage: JsonNode,
    @JsonAlias("frequency_change_fault")
    val frequencyChangeFaultPercentage: JsonNode
)
