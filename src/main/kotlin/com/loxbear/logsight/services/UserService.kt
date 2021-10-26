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
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import utils.KeyGenerator
import utils.UtilsService
import java.util.*
import java.util.concurrent.Executors

@Service
class UserService(
    val userRepository: UserRepository,
    val applicationRepository: ApplicationRepository,
    val emailService: EmailService,
    val applicationService: ApplicationService,
    val kafkaService: KafkaService,
    val paymentService: PaymentService,
    val templateEngine: TemplateEngine,
    val timeSelectionService: TimeSelectionService
    //val predefinedTimesService: PredefinedTimesService
) {
    val exceededMailSubject = "Logsight.ai Limit exceeded"
    val nearlyExceededMailSubject = "Logsight.ai Limit at 80%"

    val logger: Logger = LoggerFactory.getLogger(UserService::class.java)
    val restTemplate: RestTemplate = RestTemplateBuilder()
        .basicAuthentication("elastic", "elasticsearchpassword")
        .build()

    private val executor = Executors.newSingleThreadExecutor()

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
            "{ \"password\" : \"${user.key}\", " +
                    "\"roles\" : [\"${user.key + "_" + user.email}\"] }"
        )
        restTemplate.postForEntity<String>("http://$elasticUrl/_security/user/$userKey", request).body!!

    }

    @Transactional
    fun createUser(user: LogsightUser): LogsightUser? {
        return if (userRepository.findByEmail(user.email).isEmpty) {
            userRepository.save(user)
        } else {
            logger.warn("User with email ${user.email} already exists.")
            null
        }
    }

    fun findById(userId: Long): Optional<LogsightUser> = userRepository.findById(userId)

    fun activateUser(userActivate: UserActivateForm): LogsightUser? =
        findById(userActivate.id).map { user ->
            if (userActivate.key == user.key && !user.activated){
                executor.submit { applicationService.createApplication("compute_sample_app", user)}
                executor.submit{ applicationService.createApplication("auth_sample_app", user)}
                executor.submit{ applicationService.createApplication("auth_sample_app2", user)}
                return@map updateUser(user.copy(activated = true))
            }else
                return@map user
        }.orElse(null)



    fun changePassword(userForm: UserRegisterForm): LogsightUser? {
        return findByEmail(userForm.email).map { user ->
            updateUser(user.copy(password = encoder().encode(userForm.password)))
        }.orElse(null)
    }


    @Transactional
    fun updateUser(user: LogsightUser): LogsightUser? = userRepository.save(user)

    fun findByKey(key: String): LogsightUser =
        userRepository.findByKey(key).orElseThrow { Exception("User with key $key not found") }

    fun findAllByUser(user: LogsightUser): List<Application> = applicationRepository.findAllByUser(user)

    fun findByEmail(email: String): Optional<LogsightUser> {
        return userRepository.findByEmail(email)
    }

    fun findByStripeCustomerID(id: String): LogsightUser {
        return userRepository.findByStripeCustomerId(id).orElseThrow { Exception("User with StripeID $id not found") }
    }

    @Transactional
    @KafkaListener(topics= ["application_stats"], groupId = "")
    fun consume(message:String) :Unit {
        val privateKey = JSONObject(message).getString("private_key")
        val usedDataNow = JSONObject(message).getLong("quantity")
        logger.info("Received application user stats update message: [{}]", message)
        val user = this.findByKey(privateKey)
        val usedDataPrevious = user.usedData
        val availableData = user.availableData
        if ((usedDataPrevious + usedDataNow) > availableData){
            paymentService.updateHasPaid(user, false)
            kafkaService.updatePayment(user.key, false)
            emailService.sendMimeEmail(
                Email(
                    mailTo = user.email,
                    sub = exceededMailSubject,
                    body = getExceededLimitsMailBody(
                        "exceededLimits",
                        exceededMailSubject)
                )
            )
        }

        if ((usedDataPrevious + usedDataNow) > 0.8*availableData){
            if (!user.approachingLimit){
                paymentService.updateLimitApproaching(user, true)
                emailService.sendMimeEmail(
                    Email(
                        mailTo = user.email,
                        sub = nearlyExceededMailSubject,
                        body = getExceededLimitsMailBody(
                            "nearlyExceededLimits",
                            nearlyExceededMailSubject)
                    )
                )

            }

        }

        userRepository.updateUsedData(user.key, usedDataPrevious + usedDataNow)

    }


    private fun getExceededLimitsMailBody(
        template: String,
        title: String,
    ): String = templateEngine.process(
        template,
        with(Context()) {
            setVariable("title", title)
            this
        }
    )

    fun updateUsedData(key: String, usedData: Long) {
        val user = findByKey(key)
        val usedDataSum = user.usedData + usedData
        userRepository.updateUsedData(key, usedDataSum)
        if (usedData > user.availableData) {
            emailService.sendEmail(
                Email(
                    mailTo = user.email,
                    sub = "logsight.ai data limit exceeded",
                    body = "Your data has exceeded"
                )
            )
            kafkaService.updatePayment(user.key, false)
        }
    }

    fun existsByKey(key: String) = userRepository.existsByKey(key)
}