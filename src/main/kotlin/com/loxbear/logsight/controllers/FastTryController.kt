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
    val log: Logger = Logger.getLogger(LogService::class.java.toString())

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
        val passwd = utils.KeyGenerator.generate()
        val registerForm = UserRegisterForm(email=email, password = passwd)
        var id = 0L
        var key = ""
        var kibanaPersonalUrl = ""
        val user = userService.findByEmail(email)
        if (!user.isEmpty){
            user.map { user ->
                userService.changePassword(registerForm)
                id = user.id
                key = user.key
                kibanaPersonalUrl = "$baseUrl/kibana/s/kibana_space_${user.key}/app/kibana#/dashboards"
                for (i in applicationService.findAllByUser(user)){
                    if (i.name.contains("logsight_fast_try_app")){
                        applicationService.deleteApplication(i.id)
                    }
                }
                executor.submit { processRequest(user, fileContent, logFileType, kibanaPersonalUrl, false) }
            }
        }else{
            userService.createUser(registerForm)?.let { user ->
                id = user.id
                key = user.key
                kibanaPersonalUrl = "$baseUrl/kibana/s/kibana_space_${user.key}/app/kibana#/dashboards"
                if (elasticsearchService.createForLogsightUser(user)){
                    emailService.sendMimeEmail(
                        Email(
                            mailTo = user.email,
                            sub = registerMailSubject,
                            body = authService.getRegisterMailBody(
                                "welcomeEmail",
                                registerMailSubject,
                                URL(URL(baseUrl), "auth/login")
                            )
                        )
                    ).let { user }
                }
                userService.activateUser(UserActivateForm(user.id, user.key))
                val requestB = "{\"password\":\"${user.key}\",\"username\":\"${user.email}\"}"
                val request = UtilsService.createKibanaRequestWithHeaders(requestB)
                restTemplate.postForEntity<String>("http://$kibanaUrl/kibana/api/security/v1/login", request)
                println(file.isEmpty)
                executor.submit { processRequest(user, fileContent, logFileType, kibanaPersonalUrl, true) }
            }
        }

        return FastTryResponse(id, key, passwd, kibanaPersonalUrl)
    }



    @PreDestroy
    fun shutdonw() {
        // needed to avoid resource leak
        executor.shutdown()
    }

    fun processRequest(user: LogsightUser, fileContent: String, logFileType: String, kibanaPersonalUrl: String, isNew: Boolean) {
        if (isNew){
            val application = applicationService.createApplication("logsight_fast_try_app", user= user)
            Thread.sleep(30000)
            application?.id?.let {uploadFile(user, it, fileContent, LogFileTypes.valueOf(logFileType.toUpperCase())) }
            Thread.sleep(40000)
            applicationService.updateKibanaPatterns(user)
            emailService.sendMimeEmail(
                Email(
                    mailTo = user.email,
                    sub = "logsight.ai Lite Insights",
                    body = authService.getRegisterMailBody(
                        "logsightLiteInsights",
                        "logsight.ai Lite Insights",
                        URL(URL(kibanaPersonalUrl), "")
                    )
                )
            ).let { user }
        }else{
            Thread.sleep(30000)
            val application = applicationService.createApplication("logsight_fast_try_app", user= user)
            Thread.sleep(30000)
            application?.id?.let {uploadFile(user, it, fileContent, LogFileTypes.valueOf(logFileType.toUpperCase())) }
            Thread.sleep(40000)
            applicationService.updateKibanaPatterns(user)

            emailService.sendMimeEmail(
                Email(
                    mailTo = user.email,
                    sub = "logsight.ai Lite Insights",
                    body = authService.getRegisterMailBody(
                        "logsightLiteInsights",
                        "logsight.ai Lite Insights",
                        URL(URL(kibanaPersonalUrl), "")
                    )
                )
            ).let { user }
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