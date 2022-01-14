package com.loxbear.logsight.controllers

import com.loxbear.logsight.charts.data.LineChart
import com.loxbear.logsight.models.LogCompareTable
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.KafkaService
import com.loxbear.logsight.services.UserService
import com.loxbear.logsight.services.elasticsearch.LogCompareService
import org.json.JSONObject
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import utils.UtilsService

@RestController
@RequestMapping("/api/log_compare")
class LogCompareController(
    val logCompareService: LogCompareService,
    val userService: UserService,
    val applicationService: ApplicationService,
    val kafkaService: KafkaService
) {

    @GetMapping("/load_versions")
    fun getVersions(
        authentication: Authentication,
        @RequestParam(required = true) applicationId: Long?
    ): MutableList<String> = userService.findByEmail(authentication.name).map { user ->
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes = applicationService.getApplicationIndexesForLogCompare(user, application?.orElse(null), "log_ad")
        val applicationVersions = logCompareService.getApplicationVersions(applicationsIndexes, user)
        applicationVersions
    }.orElse(mutableListOf())

    @GetMapping("/compute_log_compare")
    fun trainLogCompareModel(
        authentication: Authentication,
        @RequestParam(required = true) applicationId: Long?,
        @RequestParam baselineTagId: String,
        @RequestParam compareTagId: String
    ): JSONObject {
        return JSONObject(
            """{
  "risk": "80",
  "total_n_log_messages": "5000",
  "count_baseline": "2500",
  "candidate_perc": "1",
  "added_states": "5",
  "added_states_info": "3",
  "added_states_fault": "2",
  "deleted_states": "10",
  "deleted_states_info": "5",
  "deleted_states_fault": "5",
  "reccuring_states": "8",
  "recurring_states_info": "3",
  "recurring_states_fault": "5",
  "frequency_change_threshold": "50",
  "frequency_change": "15",
  "frequency_change_info": "7",
  "frequency_change_fault": "8",
  "cols": [
    "risk",
    "description",
    "baseline",
    "candidate",
    "template",
    "code",
    "count",
    "change",
    "coverage",
    "level",
    "semantics"
  ],
  "rows": [
    {
      "start_date": "",
      "end_date": "2021-12-30 21:41:06.844941",
      "template": "org.apache.oozie.action.ActionExecutorException: UninitializedMessageException: Message missing required fields: renewer Caused by: com.google.protobuf.UninitializedMessageException: Message missing required fields: renewer",
      "trend_baseline": "",
      "trend_candidate": "1",
      "count_baseline": 0,
      "count_candidate": 1,
      "level": "INFO",
      "semantics": "WARNING, ERROR, EXCEPTION, CRITICAL",
      "dates": "--Dec.&nbsp30",
      "count_total": 1,
      "count_gtotal": 10287,
      "perc_baseline": 0,
      "perc_candidate": 100,
      "b_color": "rgba(255, 0, 0, 0.1)",
      "c_color": "rgba(192, 192, 192, 0.3222222222222222)",
      "change_count": "+1",
      "change_color": "rgba(0, 0, 0, 0.7)",
      "change_perc": 1,
      "coverage": 0,
      "risk_score": 100,
      "risk_description": "Added state (Fault)",
      "risk_symbol": "fa fa-exclamation-triangle font-medium-1",
      "risk_color": "rgba(255, 0, 0, 1.0)",
      "template_code": "https://github.com/apache/hadoop/blob/release-2.0.4-alpha/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-core/src/main/java/org/apache/hadoop/mapred/JobClient.java#L568",
      "count_base": "0",
      "count_cand": "1",
      "semantic_color": "rgba(255, 0, 0, 0.7)"
    }
  ]
}"""
        )
    }

    @GetMapping("/bar_plot_count")
    fun getLogCountBar(
        authentication: Authentication,
        @RequestParam startTime: String,
        @RequestParam endTime: String,
        @RequestParam(required = false) applicationId: Long,
        @RequestParam(required = false) tag: String
    ): List<LineChart> = userService.findByEmail(authentication.name).map { user ->
        val application = applicationService.findById(applicationId)
        val applicationsIndexes = applicationService.getApplicationIndexesForLogCompare(user, application?.orElse(null), "log_ad")
        val intervalAggregate = UtilsService.getTimeIntervalAggregate(startTime, endTime, 10)
        logCompareService.getLogCountBar(applicationsIndexes, startTime, endTime, user, intervalAggregate, tag)
    }.orElse(listOf())

    @GetMapping("/cognitive_bar_plot")
    fun getAnomaliesBarChartData(
        authentication: Authentication,
        @RequestParam startTime: String,
        @RequestParam endTime: String,
        @RequestParam(required = false) applicationId: Long,
        @RequestParam(required = false) tag: String
    ): List<LineChart> = userService.findByEmail(authentication.name).map { user ->
        val application = applicationService.findById(applicationId)
        val applicationsIndexes = applicationService.getApplicationIndexesForLogCompare(user, application?.orElse(null), "log_ad")
        val intervalAggregate = UtilsService.getTimeIntervalAggregate(startTime, endTime, 10)
        logCompareService.getAnomaliesBarChartData(
            applicationsIndexes,
            startTime,
            endTime,
            user,
            intervalAggregate,
            tag
        )
    }.orElse(listOf())

    @GetMapping("/compare_templates_horizontal_bar")
    fun getCompareTemplatesHorizontalBar(
        authentication: Authentication,
        @RequestParam startTime: String,
        @RequestParam endTime: String,
        @RequestParam baselineTagId: String,
        @RequestParam compareTagId: String,
        @RequestParam(required = false) applicationId: Long
    ): List<LineChart> = userService.findByEmail(authentication.name).map { user ->
        val application = applicationService.findById(applicationId)
        val applicationsIndexes = applicationService.getApplicationIndexesForLogCompare(user, application?.orElse(null), "log_ad")
        val intervalAggregate = UtilsService.getTimeIntervalAggregate(startTime, endTime, 10)
        logCompareService.getCompareTemplatesHorizontalBar(
            applicationsIndexes,
            startTime,
            endTime,
            user,
            intervalAggregate,
            baselineTagId,
            compareTagId
        )
    }.orElse(listOf())

    @GetMapping("/data")
    fun getLogCompareData(
        authentication: Authentication,
        @RequestParam startTime: String,
        @RequestParam endTime: String,
        @RequestParam(required = false) applicationId: Long?,
        @RequestParam baselineTagId: String,
        @RequestParam compareTagId: String
    ): MutableList<LogCompareTable> = userService.findByEmail(authentication.name).map { user ->
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes = applicationService.getApplicationIndexesForLogCompare(user, application?.orElse(null), "count_ad")
        logCompareService.getLogCompareData(
            applicationsIndexes,
            startTime,
            endTime,
            user,
            baselineTagId,
            compareTagId
        )
    }.orElse(mutableListOf())
}
