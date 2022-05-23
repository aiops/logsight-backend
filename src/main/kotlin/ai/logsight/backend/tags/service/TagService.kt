package ai.logsight.backend.compare.service

import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.service.ChartsService
import ai.logsight.backend.charts.ports.web.ChartsController
import ai.logsight.backend.charts.ports.web.request.ChartRequest
import ai.logsight.backend.charts.repository.ESChartRepository
import ai.logsight.backend.charts.repository.entities.elasticsearch.TagData
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.compare.controller.request.TagEntry
import ai.logsight.backend.compare.dto.Tag
import ai.logsight.backend.users.domain.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.json.JSONObject
import org.springframework.stereotype.Service

@Service
class TagService(
    private val chartsService: ChartsService,
    private val chartsRepository: ESChartRepository
) {
    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    private val logger = LoggerImpl(ChartsController::class.java)

    fun getFilterQuery(filterTags: List<TagEntry>): String {
        val filterQuery = mutableListOf<JSONObject>()
        filterTags.forEach {
            filterQuery.add(JSONObject(mapOf("match_phrase" to JSONObject(mapOf("tag_keys.keyword" to it.tagName)))))
            filterQuery.add(JSONObject(mapOf("match_phrase" to JSONObject(mapOf("tags.${it.tagName}.keyword" to it.tagValue)))))
        }
        return filterQuery.toString().drop(1).dropLast(1)
    }

    fun getCompareTagFilter(user: User, filterTags: List<TagEntry>): TagData {
        val chartRequest = ChartRequest(
            applicationId = null,
            chartConfig = ChartConfig(
                mapOf(
                    "type" to "util",
                    "feature" to "filter_tags",
                    "indexType" to "log_ad",
                    "field" to getFilterQuery(filterTags)
                )
            )
        )
        val getChartDataQuery = chartsService.getChartQuery(user.id, chartRequest)
        return mapper.readValue(chartsRepository.getData(getChartDataQuery, "*"))
    }

    fun getCompareTagValues(user: User, tagName: String): List<Tag> {
        val chartRequest = ChartRequest(
            applicationId = null,
            chartConfig = ChartConfig(
                mapOf("type" to "util", "feature" to "versions", "indexType" to "log_ad", "field" to tagName)
            )
        )
        val getChartDataQuery = chartsService.getChartQuery(user.id, chartRequest)
        val tagValues = mapper.readValue<TagData>(chartsRepository.getData(getChartDataQuery, "*"))
        return tagValues.aggregations.listAggregations.buckets.map {
            Tag(tagName = tagName, tagValue = it.tagValue, tagCount = it.tagCount)
        }
    }
}
