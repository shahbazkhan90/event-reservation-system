package com.project.eventreservation.kafka;

import com.project.eventreservation.dto.ReservationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationProducer {

    private final KafkaTemplate<String,ReservationEvent> kafkaTemplate;
    public void sendReservationEvent(ReservationEvent event){
        kafkaTemplate.send("reservation-events",event);
        System.out.println("PRODUCER: Sent event to Kafka -> " + event);
    }

}
