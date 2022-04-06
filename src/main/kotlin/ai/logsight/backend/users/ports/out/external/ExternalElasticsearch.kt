package ai.logsight.backend.users.ports.out.external

import ai.logsight.backend.connectors.elasticsearch.ElasticsearchService
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.ports.out.external.exceptions.ExternalServiceException
import org.apache.http.conn.HttpHostConnectException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.ResourceAccessException
import java.net.ConnectException

@Service
class ExternalElasticsearch(
    val elasticsearchService: ElasticsearchService
) : ExternalService {
    val logger: Logger = LoggerFactory.getLogger(ExternalElasticsearch::class.java)

    override fun initialize(user: User) {
        logger.info("Initializing elasticsearch services for user.")
        try {

            elasticsearchService.createESUser(
                username = user.email, password = user.password, roles = user.key
            )
            elasticsearchService.createKibanaSpace(user.key)
            elasticsearchService.createKibanaRole(user.key)
        } catch (e: ResourceAccessException) {
            logger.error(e.message)
            val msg =
                if (e.cause is HttpHostConnectException) "Unable to connect to elasticsearch service." else e.message
            throw ExternalServiceException(msg)
        } catch (e: ConnectException) {
            logger.error(e.message)
            throw ExternalServiceException("Unable to connect to elasticsearch service.")
        } catch (e: Exception) {
            logger.error(e.message)
            throw ExternalServiceException("Unhandled elasticsearch exception ${e.message}")
        }
    }

    override fun teardown(user: User) {
        elasticsearchService.deleteKibanaSpace(user.key)
        elasticsearchService.deleteKibanaRole(user.key)
        elasticsearchService.deleteESUser(user.email)
    }
}
