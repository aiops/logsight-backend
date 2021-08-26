package com.loxbear.logsight.services

import com.loxbear.logsight.encoder
import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.auth.Email
import com.loxbear.logsight.models.auth.UserActivateForm
import com.loxbear.logsight.models.auth.UserRegisterForm
import com.loxbear.logsight.repositories.ApplicationRepository
import com.loxbear.logsight.repositories.UserRepository
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
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

    fun createUser(userForm: UserRegisterForm): LogsightUser? =
        createUser(LogsightUser(
            id = 0,  // Will be replaced by auto-generated value
            email = userForm.email,
            password = encoder().encode(userForm.password),
            key = KeyGenerator.generate(),
        ))

    @Transactional
    fun createUser(user: LogsightUser): LogsightUser? {
        return if (userRepository.findByEmail(user.email).isEmpty) {
            userRepository.save(user)
        } else {
            logger.warn("User with email ${user.email} already exists." )
            null
        }
    }

    fun getUser(userId: Long): LogsightUser? =
        userRepository.findById(userId).map { user -> user }.orElse(null)

    fun getUser(email: String): LogsightUser? =
        userRepository.findByEmail(email).map { user -> user }.orElse(null)

    fun activateUser(userActivate: UserActivateForm): LogsightUser? =
        getUser(userActivate.id)?.let { user ->
            if(userActivate.key == user.key)
                updateUser(user.copy(activated = true))
            else
                null
        }

    fun changePassword(userForm: UserRegisterForm): LogsightUser? =
        getUser(userForm.email)?.let { user -> updateUser(user.copy(password = userForm.password)) }


    @Transactional
    fun updateUser(user: LogsightUser): LogsightUser? = userRepository.save(user)




    //createPersonalKibana(user)
    //applicationService.createApplication("compute_sample_app", user)
    //applicationService.createApplication("auth_sample_app", user)
    //applicationService.createApplication("auth2_sample_app", user)


    fun findByKey(key: String): LogsightUser =
        userRepository.findByKey(key).orElseThrow { Exception("User with key $key not found") }

    fun findAllByUser(user: LogsightUser): List<Application> = applicationRepository.findAllByUser(user)

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

    fun updateUsedData(key: String, usedData: Long) {
        val user = findByKey(key)
        val usedDataSum = user.usedData + usedData
        userRepository.updateUsedData(key, usedDataSum)
        if (usedData > user.availableData) {
            emailService.sendEmail(
                Email(
                    mailTo = user.email,
                    subject = "logsight.ai data limit exceeded",
                    body = "Your data has exceeded"
                )
            )
            kafkaService.updatePayment(user.key, false)
        }
    }

    fun existsByKey(key: String) = userRepository.existsByKey(key)
}