package com.project.eventreservation.kafka;

import com.project.eventreservation.dto.ReservationEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ReservationConsumer {

    @KafkaListener(
            topics = "reservation-events",
            groupId = "reservation-group-v10",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listener(ReservationEvent event) {
        System.out.println("CONSUMER: Received event from Kafka -> " + event);
    }
}