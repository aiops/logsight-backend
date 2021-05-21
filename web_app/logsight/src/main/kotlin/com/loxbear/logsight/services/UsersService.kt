package com.loxbear.logsight.services

import com.loxbear.logsight.encoder
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.RegisterUserForm
import com.loxbear.logsight.models.UserModel
import com.loxbear.logsight.repositories.UserRepository
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.postForEntity
import utils.KeyGenerator
import utils.UtilsService

@Service
class UsersService(val repository: UserRepository,
                   val emailService: EmailService) {

    val logger = LoggerFactory.getLogger(UsersService::class.java)
    val restTemplate = RestTemplateBuilder()
        .basicAuthentication("elastic", "elasticsearchpassword")
        .build();

    @Value("\${kibana.url}")
    private val kibanaUrl: String? = null

    @Value("\${elasticsearch.url}")
    private val elasticUrl: String? = null

    fun createUser(form: RegisterUserForm): LogsightUser {
        return with(form) {
            if (repository.findByEmail(email).isPresent) {
                throw Exception("User with email $email already exists")
            }
            repository.save(LogsightUser(id = 0, email = email, password = password, key = KeyGenerator.generate()))
        }

    }

    @Transactional
    fun registerUser(email: String): String? {
        return if (repository.findByEmail(email).isPresent) {
            return "User with email $email already exists"
        } else {
            val user = createUser(form = RegisterUserForm(email, encoder().encode("demo"), encoder().encode("demo")))
            emailService.sendActivationEmail(user)
            null
        }
    }

    fun findByKey(key: String): LogsightUser = repository.findByKey(key).orElseThrow { Exception("User with key $key not found") }

    @Transactional
    fun activateUser(key: String): UserModel {
        logger.info("Activating user with key [{}]", key)



        val user = findByKey(key)
        repository.activateUser(key)
        with(user) {
            createPersonalKibana(user)
            return UserModel(id = id, email = email, activated = activated, key = key)
        }
    }

    fun findByEmail(email: String): LogsightUser {
        return repository.findByEmail(email).orElseThrow { Exception("User with email $email not found") }
    }

    fun createPersonalKibana(user: LogsightUser){
        val userKey = user.key
        var request = UtilsService.createKibanaRequestWithHeaders(
            "{ \"id\": \"kibana_space_$userKey\", " +
                    "\"name\": \"Logsight\", " +
                    "\"description\" : \"This is your Logsight Space\" }")

        restTemplate.postForEntity<String>("http://$kibanaUrl/kibana/api/spaces/space", request).body!!

        request = UtilsService.createKibanaRequestWithHeaders(
            "{ \"metadata\" : { \"version\" : 1 }," +
                    "\"kibana\": [ { \"base\": [], \"feature\": { \"discover\": [ \"all\" ], " +
                    "\"logs\":[ \"all\" ], \"indexPatterns\": [ \"all\" ] }, " +
                    "\"spaces\": [ \"kibana_space_$userKey\" ] } ] }")
        restTemplate.put("http://$kibanaUrl/kibana/api/security/role/kibana_role_$userKey", request)

        request = UtilsService.createElasticSearchRequestWithHeaders(
            "{ \"password\" : \"test-test\", " +
                    "\"roles\" : [\"kibana_role_$userKey\"] }")
        restTemplate.postForEntity<String>("http://$elasticUrl/_security/user/$userKey", request).body!!

    }

}