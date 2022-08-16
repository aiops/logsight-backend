package ai.logsight.backend.tags.service

import ai.logsight.backend.charts.domain.service.ChartsService
import ai.logsight.backend.charts.domain.service.ESChartsServiceImpl
import ai.logsight.backend.charts.ports.web.ChartsController
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.compare.controller.request.TagEntry
import ai.logsight.backend.compare.dto.Tag
import ai.logsight.backend.compare.dto.TagKey
import ai.logsight.backend.users.domain.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.stereotype.Service

@Service
class TagService(
    private val chartsService: ChartsService,
    private val esChartsServiceImpl: ESChartsServiceImpl
) {
    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    private val logger = LoggerImpl(ChartsController::class.java)

    fun getCompareTagFilter(user: User, listTags: List<TagEntry>, applicationIndices: String): List<TagKey> {
        return esChartsServiceImpl.getCompareTagFilter(user, listTags, applicationIndices)
    }

    fun getCompareTagValues(
        user: User,
        tagName: String,
        applicationIndices: String,
        listTags: List<TagEntry>
    ): List<Tag> {
        return esChartsServiceImpl.getCompareTagValues(user, tagName, applicationIndices, listTags)
    }
}
