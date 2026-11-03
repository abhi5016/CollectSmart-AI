package com.collectsmart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CollectsmartBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                CollectsmartBackendApplication.class, args);
    }
}
