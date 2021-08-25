package com.loxbear.logsight.services

import com.loxbear.logsight.encoder
import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.UserForm
import com.loxbear.logsight.models.UserModel
import com.loxbear.logsight.repositories.ApplicationRepository
import com.loxbear.logsight.repositories.UserRepository
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.ResponseEntity
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import utils.KeyGenerator
import utils.UtilsService

@Service
class UserService(
    val userRepository: UserRepository,
    val applicationRepository: ApplicationRepository,
    val emailService: EmailService,
    val applicationService: ApplicationService,
    val kafkaService: KafkaService
) {

    val logger: Logger = LoggerFactory.getLogger(UserService::class.java)
    val restTemplate: RestTemplate = RestTemplateBuilder()
        .basicAuthentication("elastic", "elasticsearchpassword")
        .build()

    @Value("\${kibana.url}")
    private val kibanaUrl: String? = null

    @Value("\${elasticsearch.url}")
    private val elasticUrl: String? = null

    @Transactional
    fun createUser(user: UserForm): ResponseEntity<LogsightUser>? =
        userRepository.findByEmail(user.email).map { currentUser ->
            ResponseEntity.badRequest().body(currentUser)
        }.orElse(
            ResponseEntity.ok().body(userRepository.save(
                LogsightUser(
                    id = 0,  // Will be replaced by auto-generated value
                    email = user.email,
                    password = encoder().encode(KeyGenerator.generate()),
                    key = KeyGenerator.generate(),
                    loginID = KeyGenerator.generate()
                )
            ))
        )

    //createPersonalKibana(user)
    //applicationService.createApplication("compute_sample_app", user)
    //applicationService.createApplication("auth_sample_app", user)
    //applicationService.createApplication("auth2_sample_app", user)


    fun findByKey(key: String): LogsightUser =
        userRepository.findByKey(key).orElseThrow { Exception("User with key $key not found") }

    @Transactional
    fun activateUser(key: String): UserModel {
        logger.info("Activating user with key [{}]", key)
        val user = findByKey(key)
        userRepository.activateUser(key)
        with(user) {
            return UserModel(
                id = id, email = email, activated = activated, key = key, hasPaid = hasPaid,
                availableData = availableData, usedData = usedData
            )
        }
    }

    @Transactional
    fun updateUserPassword(email: String, password: String): ResponseEntity<LogsightUser> =
        userRepository.findByEmail(email).map { currentUser ->
            val updatedTask: LogsightUser =
                currentUser.copy(
                    password = password
                )
            ResponseEntity.ok().body(userRepository.save(updatedTask))
        }.orElse(ResponseEntity.notFound().build())

    @Transactional
    fun createLoginID(user: LogsightUser): String {
        val loginID = KeyGenerator.generate()
        userRepository.updateLoginID(loginID, user.key)
        return loginID
    }

    fun activateUserLoginLink(loginID: String, key: String): UserModel? {
        val user = findByKey(key)
        return if (user.loginID == loginID) {
            UserModel(
                id = user.id,
                email = user.email,
                activated = user.activated,
                key = user.key,
                hasPaid = user.hasPaid,
                availableData = user.availableData,
                usedData = user.usedData
            )
        } else {
            null
        }
    }

    fun findAllByUser(user: LogsightUser): List<Application> = applicationRepository.findAllByUser(user)

    fun getApplicationIndicesForKibana(user: LogsightUser) =
        findAllByUser(user).joinToString(",") {
            "\"${user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }}_${it.name}_parsing\", \"${
                user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }
            }_${it.name}_log_ad\", \"${
                user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }
            }_${it.name}_count_ad\", \"${
                user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }
            }_${it.name}_incidents\""
        }

    fun findByEmail(email: String): LogsightUser {
        return userRepository.findByEmail(email).orElseThrow { Exception("User with email $email not found") }
    }

    fun findByStripeCustomerID(id: String): LogsightUser {
        return userRepository.findByStripeCustomerId(id).orElseThrow { Exception("User with StripeID $id not found") }
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
                    "\"kibana\": [ { \"base\": [], \"feature\": { \"discover\": [ \"all\" ], \"visualize\": [ \"all\" ], " +
                    "\"dashboard\":  [ \"all\" ], \"advancedSettings\": [ \"all\" ], \"indexPatterns\": [ \"all\" ] }, " +
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
        userRepository.updateUsedData(key, usedDataSum)
        if (usedData > user.availableData) {
            emailService.sendAvailableDataExceededEmail(user)
            kafkaService.updatePayment(user.key, false)
        }
    }

    fun existsByKey(key: String) = userRepository.existsByKey(key)
}