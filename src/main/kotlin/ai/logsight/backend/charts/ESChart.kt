package ai.logsight.backend.charts

import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder

interface ESChart {
    fun create()
    fun buildQuery(): TermsAggregationBuilder
}
