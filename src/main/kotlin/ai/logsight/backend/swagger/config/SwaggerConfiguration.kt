package ai.logsight.backend.swagger.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.Authentication
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.ApiKey
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerConfiguration {
    @Bean
    fun api(): Docket? {
        return Docket(DocumentationType.SWAGGER_2)
            .ignoredParameterTypes(
                Authentication::class.java,
                SecurityReference::class.java,
                AuthorizationScope::class.java,
                SecurityContext::class.java
            )
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.ant("/api/v1/**"))
            .build()
            .apiInfo(apiInfo())
            .securitySchemes(listOf(apiKey()))
            .securityContexts(listOf(securityContext()))
    }

    private fun apiInfo(): ApiInfo? {
        return ApiInfoBuilder()
            .title("logsight.ai REST API")
            .version("1.0")
            .build()
    }

    private fun apiKey(): ApiKey? {
        return ApiKey("JWT", "Authorization", "header")
    }

    private fun securityContext(): SecurityContext? {
        return SecurityContext.builder().securityReferences(defaultAuth()).build()
    }

    private fun defaultAuth(): List<SecurityReference?>? {
        val authorizationScope = AuthorizationScope("global", "accessEverything")
        val authorizationScopes: Array<AuthorizationScope?> = arrayOfNulls<AuthorizationScope>(1)
        authorizationScopes[0] = authorizationScope
        return listOf(SecurityReference("JWT", authorizationScopes))
    }
}
