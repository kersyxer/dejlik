package com.project.config; // Або інший відповідний пакет

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

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

    @Bean
    public WebClient clickFlareClient(@Value("${clickflare.api-key}") String apiKey) {
        return WebClient.builder()
                .baseUrl("https://public-api.clickflare.io")
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("api-key", apiKey)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().responseTimeout(Duration.ofSeconds(60))
                ))
                .codecs(config -> config.defaultCodecs().maxInMemorySize(1024 * 1024 * 10))
                .build();
    }
}