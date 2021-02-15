package com.loxbear.logsight.repositories.elasticsearch

import com.loxbear.logsight.entities.elasticsearch.Anomaly
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AnomalyRepository : ElasticsearchRepository<Anomaly, String> {
}