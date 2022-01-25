package ai.logsight.backend.charts.domain.charts

import ai.logsight.backend.charts.ESChart
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder

class LineChart : ESChart {
    override fun create() {
        TODO("Not yet implemented")
    }

    override fun buildQuery(): TermsAggregationBuilder {
        TODO("Not yet implemented")
    }
}
