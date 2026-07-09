package com.school.sis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SisApplication {

    public static void main(String[] args) {
        SpringApplication.run(SisApplication.class, args);
    }
}
