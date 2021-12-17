package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.repositories.UserRepository
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.security.ChangePasswordRequest
import org.elasticsearch.client.security.PutUserRequest
import org.elasticsearch.client.security.RefreshPolicy
import org.elasticsearch.client.security.user.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.io.ClassPathResource
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import utils.UtilsService
import java.io.IOException
import java.util.*


@Service
class ElasticsearchService(
    val esClient: RestHighLevelClient,
    val userRepository: UserRepository,
) {

    @Value("\${elasticsearch.url}")
    private lateinit var elasticsearchUrl: String
    @Value("\${kibana.url}")
    private lateinit var kibanaUrl: String

    val restTemplate: RestTemplate = RestTemplateBuilder()
        .basicAuthentication("elastic", "elasticsearchpassword")
        .build()

    fun createForLogsightUser(user: LogsightUser): Boolean {
        val esUserBool = PutUserRequest.withPassword(
            User(user.email, Collections.singletonList(user.key + "_" + user.email)),
            user.key.toCharArray(),
            true,
            RefreshPolicy.NONE
        ).let { req ->
            try {
                esClient.security().putUser(req, RequestOptions.DEFAULT).isCreated
            } catch (e: IOException) {
                false
            }
        }

        val userKey = user.key
        var request = UtilsService.createKibanaRequestWithHeaders(
            "{ \"id\": \"kibana_space_$userKey\", " +
                    "\"name\": \"Logsight\", " +
                    "\"description\" : \"This is your Logsight Space\" }"
        )
        restTemplate.postForEntity<String>("http://$kibanaUrl/kibana/api/spaces/space", request).body!!

        request = UtilsService.createKibanaRequestWithHeaders(
            "{ \"metadata\" : { \"version\" : 1 }," +
                    "\"kibana\": [ { \"base\": [], \"feature\": { \"discover\": [ \"all\" ], \"visualize\": [ \"all\" ], " +
                    "\"dashboard\":  [ \"all\" ], \"advancedSettings\": [ \"all\" ], \"indexPatterns\": [ \"all\" ] }, " +
                    "\"spaces\": [ \"kibana_space_$userKey\" ] } ] }"
        )
        restTemplate.put("http://$kibanaUrl/kibana/api/security/role/${user.key + "_" + user.email}", request)

        return esUserBool
    }


    fun updatePassword(user: LogsightUser): Boolean =
        ChangePasswordRequest(
            user.email,
            user.key.toCharArray(),
            RefreshPolicy.NONE
        ).let { req ->
            try {
                esClient.security().changePassword(req, RequestOptions.DEFAULT)
            } catch (e: IOException) {
                false
            }
        }

    // TODO a security catastrophe... It must be a Bean somehow that is initiated on login authentication per user
    fun getClient(user: LogsightUser): RestHighLevelClient {
        val clientConfiguration = ClientConfiguration
            .builder()
            .connectedTo(elasticsearchUrl)
            .withBasicAuth(user.email, user.key)
            .build()

        return RestClients.create(clientConfiguration).rest()
    }

    /*
    TODO Everything below here needs to be refactored
     */
    fun execElasticsearchQuery(
        esIndexUserApp: String,
        startTime: String,
        stopTime: String,
        userKey: String,
        resourcePath: String
    ): String {
        val user = userRepository.findByKey(userKey).orElseThrow()
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(user.email, user.key)
            .build();
        val path = ClassPathResource(resourcePath).path
        val jsonString: String = UtilsService.readFileAsString(path)
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<String>("http://$elasticsearchUrl/$esIndexUserApp/_search", request).body!!
    }
}