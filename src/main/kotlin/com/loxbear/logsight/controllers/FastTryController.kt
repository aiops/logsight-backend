package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.entities.enums.LogFileTypes
import com.loxbear.logsight.models.ApplicationResponse
import com.loxbear.logsight.models.FastTryResponse
import com.loxbear.logsight.models.auth.Email
import com.loxbear.logsight.models.auth.UserActivateForm
import com.loxbear.logsight.models.auth.UserRegisterForm
import com.loxbear.logsight.services.*
import com.loxbear.logsight.services.elasticsearch.ElasticsearchService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import org.springframework.web.multipart.MultipartFile
import utils.UtilsService
import java.net.URL
import java.util.concurrent.Executors
import java.util.logging.Logger
import javax.annotation.PreDestroy


@RestController
@RequestMapping("/api/fast_try")
class FastTryController(
    @Value("\${app.baseUrl}") val baseUrl: String,
    @Value("\${app.baseUrlTry}") val baseUrlTry: String,

    val logService: LogService,
    val applicationService: ApplicationService,
    val userService: UserService,
    val authService: AuthService,
    val elasticsearchService: ElasticsearchService,
    val emailService: EmailService
) {

    private val executor = Executors.newSingleThreadExecutor()
    val restTemplate: RestTemplate = RestTemplateBuilder()
        .basicAuthentication("elastic", "elasticsearchpassword")
        .build()
    val logger: Logger = Logger.getLogger(LogService::class.java.toString())


    @Value("\${kibana.url}")
    private val kibanaUrl: String? = null

    @PostMapping("/{email}/{logFileType}/upload")
    private fun fastTry(
        @RequestParam("file") file: MultipartFile,
        @PathVariable email: String,
        @PathVariable logFileType: String,
    ): FastTryResponse {
        val fileContent = file.inputStream.readBytes().toString(Charsets.UTF_8)
        val registerMailSubject = "Welcome onboard!"
        var id = 0L
        var key = ""
        var kibanaPersonalUrl = ""
        var loginLinkTry = ""
        val user = userService.findByEmail(email)
        logger.info("Received a quickstart request from ${email}")
        if (!user.isEmpty){
            logger.info("The user with email $email exists.")
            user.map { user ->
                if (!user.activated){
                    logger.info("The user with email $email is still not activated, therefore activating.")
                    userService.activateUser(UserActivateForm(user.id, user.key))
                    logger.info("The user with email $email was just activated")
                }else{
                    logger.info("The user with email $email is already activated")
                }

//                userService.changePassword(registerForm)
                id = user.id
                key = user.key
//                kibanaPersonalUrl = "${baseUrlTry}kibana/s/kibana_space_${user.key}/app/kibana#/dashboards"
                kibanaPersonalUrl = "${baseUrlTry}pages/kibana"
                loginLinkTry = "${baseUrlTry}auth/login?redirect=kibana"

                for (i in applicationService.findAllByUser(user)){
                    if (i.name.contains("logsight_fast_try_app")){
                        logger.info("The user with email $email had previously tried quickstart. Deleting all previous data...")
                        applicationService.deleteApplication(i.id)
                        logger.info("The user with email $email had previously tried quickstart. Application deleted")
                    }
                }
                logger.info("Request submitted for uploading and processsing the file of the user with email $email ")

                executor.submit { processRequest(user, user.password, fileContent, logFileType, loginLinkTry, false) }
            }
        }else{
            val passwd = utils.KeyGenerator.generate()
            val registerForm = UserRegisterForm(email=email, password = passwd)
            logger.info("The user with email $email does not exists. Therefore, creating the user now.")
            userService.createUser(registerForm)?.let { user ->
                id = user.id
                key = user.key
//                kibanaPersonalUrl = "${baseUrlTry}kibana/s/kibana_space_${user.key}/app/kibana#/dashboards"
                kibanaPersonalUrl = "${baseUrlTry}pages/kibana"

                loginLinkTry = "${baseUrlTry}auth/login?redirect=kibana"

                if (elasticsearchService.createForLogsightUser(user)){
//                    emailService.sendMimeEmail(
//                        Email(
//                            mailTo = user.email,
//                            sub = registerMailSubject,
//                            body = authService.getRegisterTryBody(
//                                "welcomeEmail",
//                                registerMailSubject,
//                                passwd,
//                                URL(URL(baseUrlTry), "auth/login")
//                            )
//                        )
//                    ).let { user }

                }

                logger.info("The user with email $email does not exists. Activating the user.")
                userService.activateUser(UserActivateForm(user.id, user.key))
                val requestB = "{\"password\":\"${user.key}\",\"username\":\"${user.email}\"}"
                val request = UtilsService.createKibanaRequestWithHeaders(requestB)
                restTemplate.postForEntity<String>("http://$kibanaUrl/kibana/api/security/v1/login", request)
                logger.info("Request submitted for uploading and processsing the file of the user with email $email ")

                executor.submit { processRequest(user, passwd, fileContent, logFileType, loginLinkTry, true) }
            }
        }
        logger.info("Returning response back to the user with email $email ")
        return FastTryResponse(id, key, kibanaPersonalUrl)

    }



    @PreDestroy
    fun shutdown() {
        // needed to avoid resource leak
        executor.shutdown()
    }

    fun processRequest(user: LogsightUser, password: String, fileContent: String, logFileType: String, kibanaPersonalUrl: String, isNew: Boolean) {
        if (isNew){
            logger.info("Creating application for ${user.email}")
            val application = applicationService.createApplication("logsight_fast_try_app", user= user)
            logger.info("Sleeping 30 seconds")
            Thread.sleep(30000)
            logger.info("Uploading file")
            application?.id?.let {uploadFile(user, it, fileContent, LogFileTypes.valueOf(logFileType.toUpperCase())) }
            logger.info("Sleeping 40 seconds")
            Thread.sleep(40000)
            logger.info("Updating kibana patterns")
            applicationService.updateKibanaPatterns(user)
            logger.info("${user.email}, $kibanaPersonalUrl")
            emailService.sendMimeEmail(
                Email(
                    mailTo = user.email,
                    sub = "logsight.ai Quickstart Insights",
                    body = authService.getRegisterTryBody(
                        "welcomeEmail",
                        "logsight.ai Quickstart Insights",
                        password,
                        URL(URL(kibanaPersonalUrl), "")
                    )
                )
            )
            logger.info("Sending email to the user with email ${user.email}")
        }else{
            logger.info("Sleeping for 30 seconds for apps to be deleted.")
            Thread.sleep(30000)
            val application = applicationService.createApplication("logsight_fast_try_app", user= user)
            Thread.sleep(30000)
            logger.info("Uploading file")
            application?.id?.let {uploadFile(user, it, fileContent, LogFileTypes.valueOf(logFileType.toUpperCase())) }
            logger.info("Sleeping 40 seconds")
            Thread.sleep(40000)
            logger.info("Updating kibana patterns")
            applicationService.updateKibanaPatterns(user)
            logger.info("Finished updating kibana patterns")
            logger.info("${user.email}, $kibanaPersonalUrl")
            emailService.sendMimeEmail(
                Email(
                    mailTo = user.email,
                    sub = "logsight.ai Quickstart Insights",
                    body = authService.getRegisterTryBody(
                        "logsightLiteInsights",
                        "logsight.ai Quickstart Insights",
                        user.password,
                        URL(URL(kibanaPersonalUrl), "")
                    )
                )
            )
        }
    }


    private fun uploadFile(
        user: LogsightUser,
        appID: Long,
        fileContent: String,
        type: LogFileTypes
    ): ResponseEntity<ApplicationResponse>{
        logService.processFile(user.email, appID, fileContent, type)

        return ResponseEntity(
            ApplicationResponse(
                type="",
                title="",
                instance="",
                detail = "Data uploaded successfully.",
                status = HttpStatus.OK.value()), HttpStatus.OK
        )
    }

}