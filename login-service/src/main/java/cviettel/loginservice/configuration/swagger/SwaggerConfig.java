package cviettel.loginservice.configuration.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

//@Configuration
//public class SwaggerConfig {
//
//    @Bean
//    public GroupedOpenApi publicApi(@Value("${openapi.service.api-docs}") String apiDocs) {
//        return GroupedOpenApi.builder()
//                .group(apiDocs) // /v3/api-docs/api-service
//                .packagesToScan("cviettel.loginservice.controller")
//                .build();
//    }
//
//    @Bean
//    public OpenAPI openAPI(
//            @Value("${openapi.service.title}") String title,
//            @Value("${openapi.service.version}") String version,
//            @Value("${openapi.service.server}") String serverUrl) {
//        return new OpenAPI()
//                .servers(List.of(new Server().url(serverUrl)))
//                .info(new Info().title(title)
//                        .description("API documents")
//                        .version(version)
//                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
//    }
//}

/**
 * Configuration of swagger.
 * Swagger config will be used both for exporting Swagger UI and for OpenAPI specification generation.
 */
@Configuration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "Login Service API",
                version = "1.0",
                description = "API documents",
                license = @io.swagger.v3.oas.annotations.info.License(name = "Apache 2.0", url = "https://springdoc.org")
        ),
        servers = {
                @io.swagger.v3.oas.annotations.servers.Server(
                        url = "http://localhost:9999"
                )
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SwaggerConfig {
//    @Bean
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .info(new Info().title("Login Service API").version("1.0"));
////                .servers(List.of(new Server().url("http://localhost:9999")));
//    }

//    @Bean
//    public GroupedOpenApi publicApi() {
//        return GroupedOpenApi.builder()
//                .group("public")
//                .packagesToScan("cviettel.loginservice.controller")
////                .pathsToMatch("/api/**")
//                .build();
//    }
}