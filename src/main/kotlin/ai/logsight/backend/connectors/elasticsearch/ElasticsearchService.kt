package ai.logsight.backend.connectors.elasticsearch

import ai.logsight.backend.common.dto.Credentials
import ai.logsight.backend.connectors.elasticsearch.config.ElasticsearchConfigProperties
import ai.logsight.backend.connectors.elasticsearch.config.KibanaConfigProperties
import ai.logsight.backend.connectors.exceptions.ElasticsearchException
import ai.logsight.backend.connectors.rest.RestTemplateConnector
import ai.logsight.backend.users.domain.User
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.security.PutUserRequest
import org.elasticsearch.client.security.RefreshPolicy
import org.json.JSONObject
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import org.elasticsearch.client.security.user.User as ESUser

@Service
class ElasticsearchService(
    val client: RestHighLevelClient,
    val kibanaConfig: KibanaConfigProperties,
    val elasticsearchConfig: ElasticsearchConfigProperties
) {
    private val kibanaClient = RestTemplateConnector()

    fun createESUser(username: String, password: String, roles: String) {
        val esUser = ESUser(username, Collections.singletonList(roles))
        val request = PutUserRequest.withPassword(esUser, roles.toCharArray(), true, RefreshPolicy.NONE)
        client.security().putUser(request, RequestOptions.DEFAULT)
    }

    fun createKibanaSpace(userKey: String) {
        val query =
            "{ \"id\": \"kibana_space_${userKey}\", " + "\"name\": \"Logsight\", " + "\"description\" : \"This is your Logsight Space - ${userKey}\" }"

        val url = UriComponentsBuilder.newInstance().scheme(kibanaConfig.scheme).host(kibanaConfig.host)
            .port(kibanaConfig.port).path("/api").path("/spaces").path("/space").build().toString()
        kibanaClient.sendRequest(
            url = url, credentials = elasticsearchConfig.credentials, query = query, headerName = kibanaConfig.header
        )
    }

    fun createKibanaRole(userKey: String) {
        //        val givePermissionQuery =
//            "{ \"metadata\" : { \"version\" : 1 }, " +
//                    "\"elasticsearch\": { \"cluster\" : [ ], " +
//                    "\"indices\" : [ {\"names\" : [${getApplicationIndicesForKibana(user)}]," +
//                    " \"privileges\" : [ \"all\" ]}] }, " +
//                    "\"kibana\": [ { \"base\": [], " +
//                    "\"feature\": { \"discover\": [ \"all\" ], \"dashboard\": [ \"all\" ] , \"advancedSettings\": [ \"all\" ], \"visualize\": [ \"all\" ], \"indexPatterns\": [ \"all\" ] }, \"spaces\": [ \"kibana_space_${user.key}\" ] } ] }"

        val query =
            "{ \"metadata\" : { \"version\" : 1 }," + "\"elasticsearch\": { \"cluster\" : [ ], " + "\"indices\" : [ {\"names\" : [$userKey*]," + " \"privileges\" : [ \"all\" ]}] }, " + "\"kibana\": [ { \"base\": [], \"feature\": { \"discover\": [ \"all\" ], \"visualize\": [ \"all\" ], " + "\"dashboard\":  [ \"all\" ], \"advancedSettings\": [ \"all\" ], \"indexPatterns\": [ \"all\" ] }, " + "\"spaces\": [ \"kibana_space_${userKey}\" ] } ] }"
        val url = UriComponentsBuilder.newInstance().scheme(kibanaConfig.scheme).host(kibanaConfig.host)
            .port(kibanaConfig.port).path("/api").path("/security").path("/role").path("/$userKey")
            .build().toString()
        kibanaClient.putRequest(
            url = url, credentials = elasticsearchConfig.credentials, query = query, headerName = kibanaConfig.header
        )
    }

    fun kibanaLogin(user: User): ResponseEntity<String> {
        val query =
            "{\"providerType\":\"basic\", \"providerName\":\"basic\", \"currentURL\":\"/\", \"params\":{\"username\":\"${user.email}\", \"password\":\"${user.key}\"}}"
        val url = UriComponentsBuilder.newInstance().scheme(kibanaConfig.scheme).host(kibanaConfig.host)
            .port(kibanaConfig.port).path("/internal").path("/security").path("/login").build()
            .toString()
        return kibanaClient.postForEntity(
            url = url, credentials = Credentials(user.email, user.key), query = query, headerName = kibanaConfig.header
        )
    }

    fun createKibanaIndexPatterns(
        userKey: String,
        applicationName: String,
        indexPatterns: List<String>
    ) {
        performKibanaIndexPatternAction(userKey, applicationName, indexPatterns, delete = false)
    }

    fun deleteKibanaIndexPatterns(
        userKey: String,
        applicationName: String,
        indexPatterns: List<String>
    ) {
        performKibanaIndexPatternAction(userKey, applicationName, indexPatterns, delete = true)
    }

    private fun performKibanaIndexPatternAction(
        userKey: String,
        applicationName: String,
        indexPatterns: List<String>,
        delete: Boolean = false
    ) {

        val url = UriComponentsBuilder.newInstance().scheme(kibanaConfig.scheme).host(kibanaConfig.host)
            .port(kibanaConfig.port).path("/s").path("/kibana_space_$userKey").path("/api")
            .path("/index_patterns").path("/index_pattern").build().toString()
        try {

            indexPatterns.forEach { pattern ->
                val query =
                    "{\"override\": false,\n" + "  \"refresh_fields\": true,\n" + "  \"index_pattern\": {\n" + "     \"title\": \"${userKey}_${applicationName}_${pattern}\"\n" + "  }\n" + "}"

                if (delete) {
                    val responseEntity = kibanaClient.deleteRequest(
                        url = url,
                        credentials = elasticsearchConfig.credentials,
                        query = query,
                        headerName = kibanaConfig.header
                    )
                } else {
                    val responseEntity = kibanaClient.putRequest(
                        url = url,
                        credentials = elasticsearchConfig.credentials,
                        query = query,
                        headerName = kibanaConfig.header
                    )
                }
            }
        } catch (e: HttpClientErrorException) {
            val msgJson = JSONObject(e.responseBodyAsString)
            throw ElasticsearchException(msgJson.get("message").toString())
        }
    }
}
