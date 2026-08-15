package com.project.eventreservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventReservationApplication.class, args);
    }

}
