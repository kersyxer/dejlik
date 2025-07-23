package com.project.config; // Або інший відповідний пакет

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // Це важливо, щоб Spring знайшов цей клас
public class WebConfig implements WebMvcConfigurer { // Реалізуємо інтерфейс для налаштування Web MVC

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Застосовуємо CORS до всіх шляхів вашого API
                .allowedOrigins("http://localhost:3000") // !!! ВАЖЛИВО: Замініть на реальні домени вашого фронтенду !!!
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Дозволяємо потрібні HTTP-методи
                .allowedHeaders("*") // Дозволяємо всі заголовки (включаючи кастомні, наприклад, Authorization)
                .allowCredentials(true); // Дозволяємо надсилати куки та авторизаційні заголовки
    }
}