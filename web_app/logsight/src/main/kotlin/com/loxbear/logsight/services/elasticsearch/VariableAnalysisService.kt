package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.charts.elasticsearch.HitParam
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHit
import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.repositories.elasticsearch.VariableAnalysisRepository
import org.json.JSONObject
import org.springframework.stereotype.Service

@Service
class VariableAnalysisService(val repository: VariableAnalysisRepository) {

    fun getTemplates(es_index_user_app: String, startTime: String, stopTime: String): List<VariableAnalysisHit> {
        val resp = JSONObject(repository.getTemplates(es_index_user_app, startTime, stopTime))
        return resp.getJSONObject("hits").getJSONArray("hits").map {
            val hit = JSONObject(it.toString()).getJSONObject("_source")
            val template = hit.getString("template")
            val message = hit.getString("message")
            val params = mutableListOf<HitParam>();
            val keys: Iterator<String> = hit.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (key.startsWith("param_")) {
                    params.add(HitParam(key, hit.getString(key)))
                }
            }
            VariableAnalysisHit(message, template, params)
        }
    }

    fun getApplicationIndex(application: Application, key: String): String =
        "${key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }}_${application.name}_parsing"
}

