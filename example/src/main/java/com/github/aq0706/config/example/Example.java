package com.github.aq0706.config.example;

import com.github.aq0706.config.client.ConfigClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class Example {

    private ConfigClient configClient;

    public Example(ConfigClient configClient) {
        this.configClient = configClient;
    }

    @PostConstruct
    public void postConstruct() {
        String value = configClient.get("namespace", "appName", "key");
        System.out.println("Result:" + value);
    }
}
