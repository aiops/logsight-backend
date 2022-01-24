package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.repositories.UserRepository
import com.loxbear.logsight.services.LogService
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
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import utils.UtilsService
import java.io.IOException
import java.util.*
import java.util.logging.Logger


@Service
class ElasticsearchService(
    val esClient: RestHighLevelClient,
    val userRepository: UserRepository,
) {

    val log: Logger = Logger.getLogger(LogService::class.java.toString())

    @Value("\${elasticsearch.username}")
    private lateinit var username: String
    @Value("\${elasticsearch.password}")
    private lateinit var password: String
    @Value("\${elasticsearch.url}")
    private lateinit var elasticsearchUrl: String
    @Value("\${kibana.url}")
    private lateinit var kibanaUrl: String



    fun createForLogsightUser(user: LogsightUser): Boolean {
        val esUserBool = true
        val restTemplate: RestTemplate = RestTemplateBuilder()
            .basicAuthentication(username, password)
            .build()

        PutUserRequest.withPassword(
            User(user.email, Collections.singletonList(user.key + "_" + user.email)),
            user.key.toCharArray(),
            true,
            RefreshPolicy.NONE
        ).let { req ->
            try {
                val isCreated = esClient.security().putUser(req, RequestOptions.DEFAULT).isCreated
                log.info("Elasticsearch user created")
                isCreated
            } catch (e: IOException) {
                log.warning("Elasticsearch user creation failed: $e")
                false
            }
        }

        val userKey = user.key
        var request = UtilsService.createKibanaRequestWithHeaders(
            "{ \"id\": \"kibana_space_$userKey\", " +
                    "\"name\": \"Logsight\", " +
                    "\"description\" : \"This is your Logsight Space\" }"
        )
        try {
            log.info("Creating kibana space for user $user")
            restTemplate.postForEntity<String>("$kibanaUrl/api/spaces/space", request).statusCode.value()
            log.info("Kibana space created")
        } catch (e: HttpStatusCodeException){
            if (e.rawStatusCode == 409){
                log.info("space already exists")
                return esUserBool
            } else {
                log.warning("Failed to create kibana space: $e")
                throw e
            }
        }

        request = UtilsService.createKibanaRequestWithHeaders(
            "{ \"metadata\" : { \"version\" : 1 }," +
                    "\"kibana\": [ { \"base\": [], \"feature\": { \"discover\": [ \"all\" ], \"visualize\": [ \"all\" ], " +
                    "\"dashboard\":  [ \"all\" ], \"advancedSettings\": [ \"all\" ], \"indexPatterns\": [ \"all\" ] }, " +
                    "\"spaces\": [ \"kibana_space_$userKey\" ] } ] }"
        )
        log.info("Creating kibana role for user $user")
        try {
            restTemplate.put("$kibanaUrl/api/security/role/${user.key + "_" + user.email}", request)
            log.warning("Kibana role created")
        } catch (e: Exception) {
            log.warning("Failed to create kibana role: $e")
        }

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
            .build()
        val path = ClassPathResource(resourcePath).path
        val jsonString: String = UtilsService.readFileAsString(path)
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<String>("$elasticsearchUrl/$esIndexUserApp/_search", request).body!!
    }
}