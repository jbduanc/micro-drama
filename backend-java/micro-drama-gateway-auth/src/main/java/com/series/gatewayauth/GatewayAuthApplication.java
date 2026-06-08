package com.series.gatewayauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.series.gatewayauth", "com.series.common.auth"})
public class GatewayAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayAuthApplication.class, args);
    }
}
