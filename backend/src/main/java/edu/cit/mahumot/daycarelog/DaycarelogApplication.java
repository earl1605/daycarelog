package edu.cit.mahumot.daycarelog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DaycarelogApplication {
    public static void main(String[] args) {
        SpringApplication.run(DaycarelogApplication.class, args);
    }
}
