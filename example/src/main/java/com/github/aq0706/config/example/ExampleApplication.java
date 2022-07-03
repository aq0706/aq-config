package com.github.aq0706.config.example;

import com.github.aq0706.config.client.ConfigClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ExampleApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(ExampleApplication.class, args);

        String namespace = "namespace";
        String appName = "appName";
        String key = "key";
        String value = applicationContext.getBean(ConfigClient.class).get(namespace, appName, key);
        System.out.println("get(" + namespace + ", " + appName + ", " + key + ") = " + value);
    }

    @Bean
    public ConfigClient configClient() {
        return new ConfigClient("http://127.0.0.1", 17060);
    }
}
