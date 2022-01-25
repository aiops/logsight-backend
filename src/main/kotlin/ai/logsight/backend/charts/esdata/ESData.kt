package ai.logsight.backend.charts.esdata

import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder

interface ESData {
    fun getData(builder: TermsAggregationBuilder)
}
