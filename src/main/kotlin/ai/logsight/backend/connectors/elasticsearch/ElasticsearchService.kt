package ai.logsight.backend.connectors.elasticsearch

import ai.logsight.backend.common.dto.Credentials
import ai.logsight.backend.connectors.elasticsearch.config.ElasticsearchConfigProperties
import ai.logsight.backend.connectors.elasticsearch.config.KibanaConfigProperties
import ai.logsight.backend.connectors.rest.RestTemplateConnector
import ai.logsight.backend.users.domain.User
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.security.PutUserRequest
import org.elasticsearch.client.security.RefreshPolicy
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
    private val kibanaClient = RestTemplateConnector()

    fun createESUser(username: String, password: String, roles: String) {
        val esUser = ESUser(username, Collections.singletonList(roles))
        val request = PutUserRequest.withPassword(esUser, roles.toCharArray(), true, RefreshPolicy.NONE)
        client.security().putUser(request, RequestOptions.DEFAULT)
    }

    fun createKibanaSpace(userKey: String) {
        val query =
            "{ \"id\": \"kibana_space_${userKey}\", " + "\"name\": \"Logsight\", " + "\"description\" : \"This is your Logsight Space - ${userKey}\" }"

        val url = UriComponentsBuilder.newInstance().scheme(kibanaConfig.protocol).host(kibanaConfig.host)
            .port(kibanaConfig.port).path("/kibana").path("/api").path("/spaces").path("/space").build().toString()
        kibanaClient.sendRequest(
            url = url, credentials = elasticsearchConfig.credentials, query = query, headerName = kibanaConfig.header
        )
    }

    fun createKibanaRole(userKey: String) {
        val query =
            "{ \"metadata\" : { \"version\" : 1 }," + "\"kibana\": [ { \"base\": [], \"feature\": { \"discover\": [ \"all\" ], \"visualize\": [ \"all\" ], " + "\"dashboard\":  [ \"all\" ], \"advancedSettings\": [ \"all\" ], \"indexPatterns\": [ \"all\" ] }, " + "\"spaces\": [ \"kibana_space_${userKey}\" ] } ] }"
        val url = UriComponentsBuilder.newInstance().scheme(kibanaConfig.protocol).host(kibanaConfig.host)
            .port(kibanaConfig.port).path("/kibana").path("/api").path("/security").path("/role").path("/$userKey")
            .build().toString()
        kibanaClient.putRequest(
            url = url, credentials = elasticsearchConfig.credentials, query = query, headerName = kibanaConfig.header
        )
    }

    fun kibanaLogin(user: User): String {
        val query = "{\"providerType\":\"basic\", \"providerName\":\"basic\", \"currentURL\":\"/\", \"params\":{\"username\":\"${user.email}\", \"password\":\"${user.key}\"}}"
        val url = UriComponentsBuilder.newInstance().scheme(kibanaConfig.protocol).host(kibanaConfig.host)
            .port(kibanaConfig.port).path("/kibana").path("/internal").path("/security").path("/login")
            .build().toString()
        val response = kibanaClient.sendRequest(
            url = url, credentials = Credentials(user.email, user.key), query = query, headerName = kibanaConfig.header
        )
        return response
    }

    fun createKibanaIndexPatterns(userKey: String, applicationKey: String, indexPatterns: List<String>) {

        val url = UriComponentsBuilder.newInstance().scheme(kibanaConfig.protocol).host(kibanaConfig.host)
            .port(kibanaConfig.port).path("/kibana").path("/s").path("/kibana_space_$userKey").path("/api")
            .path("/index_patterns").path("/index_pattern").build().toString()
        println(url)
        indexPatterns.forEach { pattern ->
            val query =
                "{\"override\": false,\n" + "  \"refresh_fields\": true,\n" + "  \"index_pattern\": {\n" + "     \"title\": \"${applicationKey}_${pattern}\"\n" + "  }\n" + "}"
            kibanaClient.sendRequest(
                url = url,
                credentials = elasticsearchConfig.credentials,
                query = query,
                headerName = kibanaConfig.header
            )
        }
    }
}
