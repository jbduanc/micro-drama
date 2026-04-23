package com.tcm.rx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.tcm")
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.tcm.**.mapper")
public class RxApplication {

    public static void main(String[] args) {
        SpringApplication.run(RxApplication.class, args);
    }

}