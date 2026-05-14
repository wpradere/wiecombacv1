package com.wiimy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WiimyApplication {

    public static void main(String[] args) {
        SpringApplication.run(WiimyApplication.class, args);
    }
}
