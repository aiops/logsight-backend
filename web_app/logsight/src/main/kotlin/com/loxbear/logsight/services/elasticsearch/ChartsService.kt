package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.charts.data.DataSet
import com.loxbear.logsight.charts.data.LineChart
import com.loxbear.logsight.repositories.elasticsearch.ChartsRepository
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class ChartsService(val repository: ChartsRepository) {

    fun getLineChartData(): LineChart {
        val labels = mutableListOf<String>()
        val normal = mutableListOf<Double>()
        val anomaly = mutableListOf<Double>()
        val dataSets = mutableListOf<DataSet>()
        repository.getLineChartData().aggregations.listAggregations.buckets.forEach {
            labels.add(it.date.toBookingTime())
            val dataKey0 = it.listBuckets.buckets[0].key
            if (dataKey0 == "normal") {
                normal.add(it.listBuckets.buckets[0].docCount)
            } else if (dataKey0 == "anomaly") {
                anomaly.add(it.listBuckets.buckets[0].docCount)
            }
            if (it.listBuckets.buckets.size > 1) {
                val dataKey1 = it.listBuckets.buckets[1].key
                if (dataKey1 == "normal") {
                    normal.add(it.listBuckets.buckets[1].docCount)
                } else if (dataKey1 == "anomaly") {
                    anomaly.add(it.listBuckets.buckets[1].docCount)
                }
            }
        }
        dataSets.add(DataSet(data = anomaly, label = "Anomaly"))
        dataSets.add(DataSet(data = normal, label = "Normal"))
        return LineChart(labels = labels, datasets = dataSets)
    }

    fun ZonedDateTime.toBookingTime(): String = this.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

}