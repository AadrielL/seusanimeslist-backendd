package com.seusanimes.config; // Certifique-se de que o pacote está correto

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration // Indica que esta classe contém definições de beans
public class AppConfig {

    @Bean // Indica que o método retorna um bean gerenciado pelo Spring
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}