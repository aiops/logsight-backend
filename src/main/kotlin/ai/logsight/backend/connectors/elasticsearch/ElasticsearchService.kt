package ai.logsight.backend.connectors.elasticsearch

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
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import org.elasticsearch.client.security.user.User as ESUser

@Service
class ElasticsearchService(
    val client: RestHighLevelClient,
    val kibanaConfig: KibanaConfigProperties,
    val elasticsearchConfig: ElasticsearchConfigProperties
) {
    val logger: Logger = LoggerFactory.getLogger(ElasticsearchService::class.java)

    private val kibanaClient = RestTemplateConnector()

    fun createESUser(username: String, password: String, roles: String) {
        logger.debug("Creating elasticsearch user $username.")
        val esUser = ESUser(username, Collections.singletonList(roles))
        val request = PutUserRequest.withPassword(esUser, roles.toCharArray(), true, RefreshPolicy.NONE)
        client.security().putUser(request, RequestOptions.DEFAULT)
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

    fun updateFieldByIndexAndDocID(parameters: HashMap<String, Any>, index: String, id: String): String {
        val request = UpdateRequest(
            index, id
        ).doc(parameters)

        val updateResponse: UpdateResponse = client.update(
            request, RequestOptions.DEFAULT
        )
        val responseId: String = updateResponse.id
        if (updateResponse.result === DocWriteResponse.Result.UPDATED) {
            return responseId
        } else {
            throw ElasticsearchException("Update was not successful. Please try again.")
        }
    }

    fun deleteESUser(username: String) {
        logger.info("Deleting elasticsearch user $username.")
        client.security().deleteUser(DeleteUserRequest(username), RequestOptions.DEFAULT)
    }

    fun deleteKibanaRole(userKey: String) {
        logger.info("Deleting kibana roles for personal space and index patterns for user $userKey.")
        val url = UriComponentsBuilder.newInstance().scheme(kibanaConfig.scheme).host(kibanaConfig.host)
            .port(kibanaConfig.port).path("/api").path("/security").path("/role").path("/$userKey").build().toString()
        kibanaClient.deleteRequest(
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
        kibanaClient.sendRequest(
            url = url, credentials = elasticsearchConfig.credentials, query = query, headerName = kibanaConfig.header
        )
    }

    fun deleteKibanaSpace(userKey: String) {
        logger.info("Deleting kibana space $userKey.")
        val url = UriComponentsBuilder.newInstance().scheme(kibanaConfig.scheme).host(kibanaConfig.host)
            .port(kibanaConfig.port).path("/api").path("/spaces").path("/space").path("/kibana_space_$userKey").build()
            .toString()
        kibanaClient.deleteRequest(
            url = url, credentials = elasticsearchConfig.credentials, query = null, headerName = kibanaConfig.header
        )
    }

    fun createKibanaRole(userKey: String) {
        logger.info("Creating kibana role $userKey for personal space and index patterns.")

        val query =
            "{ \"metadata\" : { \"version\" : 1 }," + "\"elasticsearch\": { \"cluster\" : [ ], " + "\"indices\" : [ {\"names\" : [$userKey*]," + " \"privileges\" : [ \"all\" ]}] }, " + "\"kibana\": [ { \"base\": [], \"feature\": { \"discover\": [ \"all\" ], \"visualize\": [ \"all\" ], " + "\"dashboard\":  [ \"all\" ], \"advancedSettings\": [ \"all\" ], \"indexPatterns\": [ \"all\" ] }, " + "\"spaces\": [ \"kibana_space_${userKey}\" ] } ] }"
        val url = UriComponentsBuilder.newInstance().scheme(kibanaConfig.scheme).host(kibanaConfig.host)
            .port(kibanaConfig.port).path("/api").path("/security").path("/role").path("/$userKey").build().toString()
        kibanaClient.putRequest(
            url = url, credentials = elasticsearchConfig.credentials, query = query, headerName = kibanaConfig.header
        )
    }
}
