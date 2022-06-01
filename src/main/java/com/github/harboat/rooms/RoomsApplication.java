package com.github.harboat.rooms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication(
        scanBasePackages = {
                "com.github.harboat.rabbitmq",
                "com.github.harboat.rooms"
        }
)
@EnableEurekaClient
public class RoomsApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoomsApplication.class, args);
    }
}
