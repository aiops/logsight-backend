// package ai.logsight.backend.charts
//
// import ai.logsight.backend.charts.esdata.ESData
// import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder
// import java.util.*
//
// class BarChart : ESChart {
//    override fun create() {
//        TODO("Not yet implemented")
//    }
//
//    override fun buildQuery(): TermsAggregationBuilder {
//        TODO("Not yet implemented")
//    }
// }
//
// // ChartBuilder.chartType(BarChart).data
//
// // BarChart().setData(DataQuery)
//
// class ChartBuilder() {
//    private var chartMapper: Map<String, ESChart> = mapOf(
//        "bar" to BarChart(), "line" to LineChart(), "pie" to PieChart()
//    )
//    private var data: ESData? = null
//    private var chart: ESChart? = null
//    private var context: String? = null
//
//    fun chartType(chart: String): ChartBuilder {
//        this.chart = chartMapper[chart]
//        return this
//    }
//
//    fun setData(data: ESData): ChartBuilder {
//        this.data = data
//        return this
//    }
//
//    fun build(): ESChart {
//        this.data.getData(this.chart.buildQuery())
//    }
// }
