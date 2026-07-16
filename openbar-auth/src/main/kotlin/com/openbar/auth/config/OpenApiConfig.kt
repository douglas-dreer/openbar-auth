package com.openbar.auth.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("OPENBAR Auth API")
                    .description(
                        """
                        Microserviço de identidade e acesso do OPENBAR.
                        
                        Responsável por:
                        - Autenticação via JWT (login)
                        - CRUD de usuários (funcionários)
                        
                        Roles disponíveis: ADMIN, MANAGER, WAITER, CASHIER, KITCHEN
                        """.trimIndent()
                    )
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("OPENBAR Team")
                            .email("dev@openbar.com")
                    )
                    .license(
                        License()
                            .name("Apache 2.0")
                            .url("https://www.apache.org/licenses/LICENSE-2.0")
                    )
            )
            .servers(
                listOf(
                    Server().url("http://localhost:8081").description("Local Dev"),
                    Server().url("http://localhost:8081").description("Docker Compose")
                )
            )
    }
}
