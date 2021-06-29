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
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.postForEntity
import utils.KeyGenerator
import utils.UtilsService

@Service
class UsersService(
    val repository: UserRepository,
    val emailService: EmailService,
    val applicationService: ApplicationService,
    val kafkaService: KafkaService
) {

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
            val user =
                repository.save(LogsightUser(id = 0, email = email, password = password, key = KeyGenerator.generate()))
            createPersonalKibana(user)
            applicationService.createApplication("compute_sample_app", user)
            applicationService.createApplication("auth_sample_app", user)
            applicationService.createApplication("auth2_sample_app", user)
            user
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

    fun findByKey(key: String): LogsightUser =
        repository.findByKey(key).orElseThrow { Exception("User with key $key not found") }

    @Transactional
    fun activateUser(key: String): UserModel {
        logger.info("Activating user with key [{}]", key)
        val user = findByKey(key)
        repository.activateUser(key)
        with(user) {
            return UserModel(id = id, email = email, activated = activated, key = key, hasPaid = hasPaid,
                availableData = availableData, usedData = usedData)
        }
    }

    @Transactional
    fun createLoginID(user: LogsightUser): String {
        val loginID = KeyGenerator.generate()
        repository.updateLoginID(loginID, user.key)
        return loginID
    }

    fun activateUserLoginLink(loginID: String, key: String): UserModel? {
        val user = findByKey(key)

        if (user.loginID == loginID){
            with(user) {
                return UserModel(
                    id = user.id,
                    email = user.email,
                    activated = user.activated,
                    key = user.key,
                    hasPaid = user.hasPaid,
                    availableData = user.availableData,
                    usedData = user.usedData
                )
            }
        }
        else{
            return null
        }
    }


    fun findByEmail(email: String): LogsightUser {
        return repository.findByEmail(email).orElseThrow { Exception("User with email $email not found") }
    }

    fun findByStripeCustomerID(id: String): LogsightUser {
        return repository.findByStripeCustomerId(id).orElseThrow { Exception("User with StripeID $id not found") }
    }

    fun createPersonalKibana(user: LogsightUser) {
        val userKey = user.key
        var request = UtilsService.createKibanaRequestWithHeaders(
            "{ \"id\": \"kibana_space_$userKey\", " +
                "\"name\": \"Logsight\", " +
                "\"description\" : \"This is your Logsight Space\" }"
        )

        restTemplate.postForEntity<String>("http://$kibanaUrl/kibana/api/spaces/space", request).body!!

        request = UtilsService.createKibanaRequestWithHeaders(
            "{ \"metadata\" : { \"version\" : 1 }," +
                "\"kibana\": [ { \"base\": [], \"feature\": { \"discover\": [ \"all\" ], " +
                "\"logs\":[ \"all\" ], \"indexPatterns\": [ \"all\" ] }, " +
                "\"spaces\": [ \"kibana_space_$userKey\" ] } ] }"
        )
        restTemplate.put("http://$kibanaUrl/kibana/api/security/role/kibana_role_$userKey", request)

        request = UtilsService.createElasticSearchRequestWithHeaders(
            "{ \"password\" : \"test-test\", " +
                "\"roles\" : [\"kibana_role_$userKey\"] }"
        )
        restTemplate.postForEntity<String>("http://$elasticUrl/_security/user/$userKey", request).body!!

    }
    @Transactional
    @KafkaListener(topics = ["application_stats"])
    fun applicationStatsChange(message: String) {
        val json = JSONObject(message)
        updateUsedData(json.getString("private_key"), json.getLong("quantity"))
    }

    @Transactional
    fun updateUsedData(key: String, usedData: Long) {
        val user = findByKey(key)
        val usedDataSum = user.usedData + usedData
        repository.updateUsedData(key, usedDataSum)
        if (usedData > user.availableData) {
            emailService.sendAvailableDataExceededEmail(user)
            kafkaService.updatePayment(user.key, false)
        }
    }

}