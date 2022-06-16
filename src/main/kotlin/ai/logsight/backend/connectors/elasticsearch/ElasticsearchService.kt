package ai.logsight.backend.connectors.elasticsearch

import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.repository.builders.ESQueryBuilder
import ai.logsight.backend.common.dto.Credentials
import ai.logsight.backend.connectors.elasticsearch.config.ElasticsearchConfigProperties
import ai.logsight.backend.connectors.elasticsearch.config.KibanaConfigProperties
import ai.logsight.backend.connectors.rest.RestTemplateConnector
import org.elasticsearch.action.DocWriteResponse
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.action.update.UpdateResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.security.DeleteUserRequest
import org.elasticsearch.client.security.PutUserRequest
import org.elasticsearch.client.security.RefreshPolicy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import org.elasticsearch.client.security.user.User as ESUser

@Service
class ElasticsearchService(
    val client: RestHighLevelClient,
    val kibanaConfig: KibanaConfigProperties,
    val elasticsearchConfig: ElasticsearchConfigProperties,
    val restConnector: RestTemplateConnector = RestTemplateConnector()
) {
    val logger: Logger = LoggerFactory.getLogger(ElasticsearchService::class.java)


    fun createESUser(username: String, password: String, roles: String) {
        logger.debug("Creating elasticsearch user $username.")
        val esUser = ESUser(username, Collections.singletonList(roles))
        val request = PutUserRequest.withPassword(esUser, roles.toCharArray(), true, RefreshPolicy.NONE)
        try {
            client.security().putUser(request, RequestOptions.DEFAULT)
        } catch (e: HttpClientErrorException.Conflict) {
            logger.warn("Elasticsearch user $username already exists.")
        }
    }

    fun getData(getDataQuery: GetChartDataQuery, index: String): String {
        val chartConfig = getDataQuery.chartConfig
        val query = ESQueryBuilder().buildQuery(
            chartConfig.parameters as Map<String, String>
        )
        val url = UriComponentsBuilder.newInstance().scheme(elasticsearchConfig.scheme).host(elasticsearchConfig.host)
            .port(elasticsearchConfig.port).path(index).path("/_search").build().toString()
        return try {
            restConnector.sendRequest(
                url, Credentials(getDataQuery.user.email, getDataQuery.user.key), query
            ).body!!
        } catch (e: HttpClientErrorException){
            when(e) {
                is HttpClientErrorException.Forbidden -> {
                    initESUser(getDataQuery.user.email, getDataQuery.user.key)
                }
                is HttpClientErrorException.Unauthorized -> {
                    initESUser(getDataQuery.user.email, getDataQuery.user.key)
                }
            }
            restConnector.sendRequest(
                url, Credentials(getDataQuery.user.email, getDataQuery.user.key), query
            ).body!!
        }
    }

    fun initESUser(email: String, userKey: String){
        createESUser(email, userKey, userKey)
        createKibanaSpace(userKey)
        createKibanaRole(userKey)
    }

    fun deleteByIndexAndDocID(index: String, id: String): String {
        val request = DeleteRequest(
            index, id
        )
        val deleteResponse: DeleteResponse = client.delete(
            request, RequestOptions.DEFAULT
        )
        val deletedId = deleteResponse.id
        if (deleteResponse.result == DocWriteResponse.Result.NOT_FOUND) {
            throw ElasticsearchException("The document does not exists in elasticsearch. Please check your index and document ID and try again")
        }
        return deletedId
    }

    fun updateFieldsByIndexAndDocID(parameters: Map<String, Any>, index: String, id: String): String {
        val request = UpdateRequest(
            index, id
        ).doc(parameters)

        val updateResponse: UpdateResponse = client.update(
            request, RequestOptions.DEFAULT
        )
        return updateResponse.id
    }

    fun deleteESUser(username: String) {
        logger.info("Deleting elasticsearch user $username.")
        client.security().deleteUser(DeleteUserRequest(username), RequestOptions.DEFAULT)
    }

    fun deleteKibanaRole(userKey: String) {
        logger.info("Deleting kibana roles for personal space and index patterns for user $userKey.")
        val url = UriComponentsBuilder.newInstance().scheme(kibanaConfig.scheme).host(kibanaConfig.host)
            .port(kibanaConfig.port).path("/api").path("/security").path("/role").path("/$userKey").build().toString()
        restConnector.deleteRequest(
            url = url, credentials = elasticsearchConfig.credentials, query = null, headerName = kibanaConfig.header
        )
    }

    fun deleteESIndices(index: String) {
        try {
            client.indices().delete(DeleteIndexRequest("$index*"), RequestOptions.DEFAULT)
        } catch (e: Exception) {
            throw ElasticsearchException("Failed to delete elasticsearch index $index. Reason: $e")
        }
    }

    fun createKibanaSpace(userKey: String) {
        logger.info("Creating kibana space $userKey.")
        val query =
            "{ \"id\": \"kibana_space_${userKey}\", " + "\"name\": \"Logsight\", " + "\"description\" : \"This is your Logsight Space - ${userKey}\" }"

        val url = UriComponentsBuilder.newInstance().scheme(kibanaConfig.scheme).host(kibanaConfig.host)
            .port(kibanaConfig.port).path("/api").path("/spaces").path("/space").build().toString()
        try {
            restConnector.sendRequest(
                url = url,
                credentials = elasticsearchConfig.credentials,
                query = query,
                headerName = kibanaConfig.header
            )
        } catch (e: HttpClientErrorException.Conflict) {
            logger.warn("Kibana space for user with key $userKey already exists.")
        }

    }

    fun deleteKibanaSpace(userKey: String) {
        logger.info("Deleting kibana space $userKey.")
        val url = UriComponentsBuilder.newInstance().scheme(kibanaConfig.scheme).host(kibanaConfig.host)
            .port(kibanaConfig.port).path("/api").path("/spaces").path("/space").path("/kibana_space_$userKey").build()
            .toString()
        restConnector.deleteRequest(
            url = url, credentials = elasticsearchConfig.credentials, query = null, headerName = kibanaConfig.header
        )
    }

    fun createKibanaRole(userKey: String) {
        logger.info("Creating kibana role $userKey for personal space and index patterns.")
        val query =
            "{ \"metadata\" : { \"version\" : 1 }," + "\"elasticsearch\": { \"cluster\" : [ ], " + "\"indices\" : [ {\"names\" : [$userKey*]," + " \"privileges\" : [ \"all\" ]}] }, " + "\"kibana\": [ { \"base\": [], \"feature\": { \"discover\": [ \"all\" ], \"visualize\": [ \"all\" ], " + "\"dashboard\":  [ \"all\" ], \"advancedSettings\": [ \"all\" ], \"indexPatterns\": [ \"all\" ] }, " + "\"spaces\": [ \"kibana_space_${userKey}\" ] } ] }"
        val url = UriComponentsBuilder.newInstance().scheme(kibanaConfig.scheme).host(kibanaConfig.host)
            .port(kibanaConfig.port).path("/api").path("/security").path("/role").path("/$userKey").build().toString()
        try {
            restConnector.putRequest(
                url = url,
                credentials = elasticsearchConfig.credentials,
                query = query,
                headerName = kibanaConfig.header
            )
        } catch (e: HttpClientErrorException.Conflict) {
            logger.warn("Kibana role for user with key $userKey already exists.")
        }

    }
}
