package com.portfolio.ecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "Bearer Authentication";

    @Bean
    public OpenAPI ecommerceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce Catalog API")
                        .description(
                                "RESTful API for managing an e-commerce product catalog. " +
                                "**GET** endpoints are public. **POST / PUT / DELETE** require a JWT token.\n\n" +
                                "**How to authenticate:** call `POST /auth/register` or `POST /auth/login`, " +
                                "copy the token, click **Authorize** above, and paste it in."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Portfolio Project")
                                .url("https://github.com/YOUR_USERNAME/ecommerce-catalog")))

                // Adds a global "Authorize" padlock button to the Swagger UI
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT token obtained from POST /auth/login")));
    }
}
