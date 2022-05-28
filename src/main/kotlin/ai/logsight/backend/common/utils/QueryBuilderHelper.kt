package ai.logsight.backend.common.utils

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.compare.controller.request.TagEntry
import ai.logsight.backend.users.domain.User
import org.json.JSONObject
import org.springframework.stereotype.Service

@Service
class QueryBuilderHelper() {
    fun getBaselineTagsQuery(baselineTags: Map<String, String>): String {
        val filterQuery = mutableListOf<JSONObject>()
        baselineTags.forEach {
            filterQuery.add(JSONObject(mapOf("match_phrase" to JSONObject(mapOf("baseline_tags.${it.key}.keyword" to it.value)))))
        }
        return filterQuery.toString().drop(1).dropLast(1)
    }
    fun getTagsFilterQuery(filterTags: List<TagEntry>, applicationIndices: String): String {
        val filterQuery = mutableListOf<JSONObject>()
        if (applicationIndices.isNotEmpty()) {
            filterTags.forEach {
                filterQuery.add(JSONObject(mapOf("match_phrase" to JSONObject(mapOf("tag_keys.keyword" to it.tagName)))))
                filterQuery.add(JSONObject(mapOf("match_phrase" to JSONObject(mapOf("tags.${it.tagName}.keyword" to it.tagValue)))))
            }
        } else {
            filterTags.forEach {
                filterQuery.add(JSONObject(mapOf("match_phrase" to JSONObject(mapOf("baseline_tag_keys.keyword" to it.tagName)))))
                filterQuery.add(JSONObject(mapOf("match_phrase" to JSONObject(mapOf("baseline_tags.${it.tagName}.keyword" to it.tagValue)))))
            }
        }

        return filterQuery.toString().drop(1).dropLast(1)
    }
}
