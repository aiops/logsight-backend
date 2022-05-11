package ai.logsight.backend.users.ports.out.external

import ai.logsight.backend.connectors.elasticsearch.ElasticsearchService
import ai.logsight.backend.users.domain.service.UserService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@RestController
@RequestMapping("/api/v1/external")
class ExternalController(
    private val userService: UserService,
    val elasticsearchService: ElasticsearchService
) {

//    @PostMapping("/kibana/login")
//    @ResponseStatus(HttpStatus.OK)
//    fun kibanaLogin(
//        authentication: Authentication
//    ): ResponseEntity<String> {
//        val user = userService.findUser()
//        return elasticsearchService.kibanaLogin(user)
//    }
}
