package com.droneanalytics.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Drone Analytics System API")
                        .description("""
                            ## 🚀 Система аналитики полетов гражданских БПЛА
                            
                            REST API для анализа количества и длительности полетов гражданских
                            беспилотников в регионах Российской Федерации.
                            
                            ### 🔐 Аутентификация
                            Для доступа к защищенным endpoint'ам необходимо:
                            1. Вызвать `/api/auth/login` для получения JWT токена
                            2. Добавить заголовок: `Authorization: Bearer <your_token>`
                            
                            ### 👥 Роли пользователей
                            - **ADMIN**: Полный доступ (просмотр + загрузка данных + управление пользователями)
                            - **ANALYST**: Только просмотр данных и аналитика
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BRZteam")
                                .email("support@brzteam.ru")
                                .url("https://brzteam.ru"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
