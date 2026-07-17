package com.openbar.pm.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("OPENBAR Product Manager API")
                    .description("API de gestão de categorias e produtos do OPENBAR")
                    .version("1.0.0")
            )
    }
}
