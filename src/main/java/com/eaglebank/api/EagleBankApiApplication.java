package com.eaglebank.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class EagleBankApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EagleBankApiApplication.class, args);
    }
}

