package ai.logsight.backend.users.ports.out.external

import ai.logsight.backend.common.dto.Credentials
import ai.logsight.backend.connectors.elasticsearch.ElasticsearchService
import ai.logsight.backend.connectors.elasticsearch.config.KibanaConfigProperties
import ai.logsight.backend.connectors.rest.RestTemplateConnector
import ai.logsight.backend.users.domain.service.UserService
import ai.logsight.backend.users.domain.service.command.*
import ai.logsight.backend.users.domain.service.query.FindUserByEmailQuery
import ai.logsight.backend.users.ports.web.request.*
import ai.logsight.backend.users.ports.web.response.*
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.postForEntity

@RestController
@RequestMapping("/api/v1/external")
class ExternalController(
    private val userService: UserService,
    val elasticsearchService: ElasticsearchService
) {

    @PostMapping("/kibana/login")
    @ResponseStatus(HttpStatus.OK)
    fun kibanaLogin(
        authentication: Authentication
    ): String {
        val user = userService.findUserByEmail(FindUserByEmailQuery(authentication.name))
        return elasticsearchService.kibanaLogin(user)
    }
}
