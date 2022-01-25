package ai.logsight.backend

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.persistence.Id

@JsonIgnoreProperties(ignoreUnknown = true)
data class Test(
    @JsonAlias("@timestamp") @Id val timestamp: String,
    val actual_level: String
)

// interface UserESRepository : ElasticsearchRepository<Test, String> {
//
//    fun findByMessage(message: String): Test
// }

@RestController
@RequestMapping("/test")
class TestController(
    val client: RestHighLevelClient,
    val elasticsearchRestTemplate: ElasticsearchRestTemplate
) {

    @GetMapping("/{message}")
    fun findById(@PathVariable("message") message: String): String? {
        val index = "gmmlbirrlud46szjhax99imhok_jbossjson_log_ad"
        val searchRequest = SearchRequest(index)
        val searchSourceBuilder = SearchSourceBuilder()
        val aggregationBuilder = AggregationBuilders.global("agg")
            .subAggregation(AggregationBuilders.dateHistogram("@timestamp"))

        searchSourceBuilder.query(QueryBuilders.termQuery("actual_level", "warn"))
        searchRequest.source(searchSourceBuilder)
        return searchRequest.toString()
//        return client.search(searchRequest, RequestOptions.DEFAULT).hits.hits[0].toString()
//        return repository.findByMessage("qBYlX34BY-7k1AR4uTLq").toString()
    }
}
