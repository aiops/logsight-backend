package com.loxbear.logsight.entities.elasticsearch

import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import javax.persistence.*

@Document(indexName = "log_anomaly_detection")
data class Anomaly(
    @Id
    val id: String,

    @Field(name = "actual_level", type = FieldType.Text)
    val actualLevel: String?,

    @Field(name = "message", type = FieldType.Text)
    val message: String?,

    @Field(name = "name", type = FieldType.Text)
    val name: String?,

    @Field(name = "prediction", type = FieldType.Text)
    val prediction: String?,

    @Field(name = "source", type = FieldType.Text)
    val source: String?,
)