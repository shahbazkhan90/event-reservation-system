package com.project.eventreservation.kafka;

import com.project.eventreservation.dto.ReservationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ReservationConsumer {

//    @KafkaListener(
//            topics = "reservation-events",
//            groupId = "reservation-group-v10",
//            containerFactory = "kafkaListenerContainerFactory"
//    )
//    public void listener(ReservationEvent event) {
//        System.out.println("CONSUMER: Received event from Kafka -> " + event);
//    }

    private static final Logger log = LoggerFactory.getLogger(ReservationConsumer.class);

    @KafkaListener(topics = "reservation-events" , groupId = "reservation-group-v10")
    public void consumeReservationEvent(ReservationEvent event){
        log.info("Recieved ReservationEvent from main topic : {}" ,event);

        if(event.getUserId() != null && event.getUserId().equals(999L)){
            log.error("Downstream notification service is unreachable for userId: 999! Throwing Exception ...");
            throw new RuntimeException("Simulated downstream processing failure for userId:"+ event.getUserId());
        }

        log.info("Successfully processed post-reservation workflows for reservation  ID: {}",event.getReservationId());

    }

    @KafkaListener(topics = "reservation-events.DLT",groupId = "reservation-dlt-group")
    public void consumeDeadletterEvent(ReservationEvent event){
        log.warn("Alert: Processing dead-lettered message from reservaiton-event.DLT for reservation ID: {}",event.getReservationId());
    }

}