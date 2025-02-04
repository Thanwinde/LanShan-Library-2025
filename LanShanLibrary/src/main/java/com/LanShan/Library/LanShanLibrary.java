package com.LanShan.Library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class LanShanLibrary {

    public static void main(String[] args) {
        SpringApplication.run(LanShanLibrary.class, args);
    }

}
