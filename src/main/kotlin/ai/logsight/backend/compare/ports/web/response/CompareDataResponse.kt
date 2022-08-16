package ai.logsight.backend.compare.ports.web.response

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.JsonNode

@JsonIgnoreProperties(ignoreUnknown = true)
data class CompareDataResponse(
    var link: String? = "",
    var baselineTags: Map<String, String> = mapOf(),
    var candidateTags: Map<String, String> = mapOf(),
    @JsonAlias("compareId")
    val compareId: String,
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
