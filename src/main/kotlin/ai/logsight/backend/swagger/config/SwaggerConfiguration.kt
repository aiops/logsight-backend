package ai.logsight.backend.swagger.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.ApiKey
import springfox.documentation.service.SecurityScheme
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.util.*

@Configuration
@EnableSwagger2
class SwaggerConfiguration {

//    @Bean
//    fun api(): Docket = Docket(DocumentationType.SWAGGER_2)
//        .select()
//        .apis(RequestHandlerSelectors.any())
//        .paths(PathSelectors.ant("/api/v1/**"))
//        .build()

    @Bean
    fun api(): Docket? {
        return Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.ant("/api/v1/**"))
            .build()
            .apiInfo(apiInfo())
            .securitySchemes(Arrays.asList(apiKey()) as List<SecurityScheme>?)
    }

    private fun apiInfo(): ApiInfo? {
        return ApiInfoBuilder()
            .title("logsight.ai REST API")
            .termsOfServiceUrl("localhost")
            .version("1.0")
            .build()
    }

    private fun apiKey(): ApiKey? {
        return ApiKey("jwtToken", "Authorization", "header")
    }
}
