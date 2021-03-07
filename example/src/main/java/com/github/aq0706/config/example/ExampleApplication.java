package com.github.aq0706.config.example;

import com.github.aq0706.config.client.ConfigClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @Bean
    public ConfigClient configClient() {
        return new ConfigClient("http://127.0.0.1", 17060);
    }
}
