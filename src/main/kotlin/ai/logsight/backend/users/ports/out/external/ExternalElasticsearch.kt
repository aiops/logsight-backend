package ai.logsight.backend.users.ports.out.external

import ai.logsight.backend.connectors.elasticsearch.ElasticsearchService
import ai.logsight.backend.users.domain.User
import org.springframework.stereotype.Service

@Service
class ExternalElasticsearch(
    val elasticsearchService: ElasticsearchService
) : ExternalService {

    override fun initialize(user: User) {
        elasticsearchService.createESUser(
            username = user.email, password = user.password, roles = user.key
        )
        elasticsearchService.createKibanaRole(user.key)
        elasticsearchService.createKibanaSpace(user.key)
    }
}
