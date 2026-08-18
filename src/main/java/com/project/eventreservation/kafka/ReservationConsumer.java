package com.project.eventreservation.kafka;

import com.project.eventreservation.dto.ReservationEvent;
import com.project.eventreservation.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ReservationConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReservationConsumer.class);
    private final NotificationService notificationService;

    public ReservationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "reservation-events" , groupId = "reservation-group-v10")
    public void consumeReservationEvent(ReservationEvent event){
        log.info("[KAFKA CONSUMER] Received Event for Reservation ID: {} | Status: {}", event.getReservationId(), event.getStatus());

        // 1. The Chaos Tripwire: Retained to physically test DLQ routing
        if(event.getUserId() != null && event.getUserId().equals(999L)){
            log.error("Downstream notification service is unreachable for userId: 999! Throwing Exception ...");
            throw new RuntimeException("Simulated downstream processing failure for userId:"+ event.getUserId());
        }

        // 2. The Notification Routing Switchboard
        try {
            switch (event.getStatus()) {
                case "PENDING":
                    // Covers both initial bookings and waitlist promotions
                    notificationService.sendPaymentLink(event.getUserId(), event.getReservationId(), event.getEventId());
                    break;
                case "CONFIRMED":
                    log.info("[TICKET ISSUED] Simulating PDF generation for Reservation ID: {}", event.getReservationId());
                    break;
                case "CANCELLED":
                    notificationService.sendCancellationNotice(event.getUserId(), event.getReservationId());
                    break;
                case "WAITLISTED":
                    log.info("[WAITLIST] User {} added to the waitlist queue for Event {}", event.getUserId(), event.getEventId());
                    break;
                default:
                    log.warn("Unknown status received: {}", event.getStatus());
            }
            log.info("Successfully processed post-reservation workflows for reservation ID: {}", event.getReservationId());
        } catch (Exception e) {
            log.error("Failed to process notification for Reservation ID: {}", event.getReservationId(), e);
            // If the notification service throws an error, we bubble it up so Kafka can retry or route to DLT
            throw new RuntimeException("Consumer processing failed", e);
        }
    }

    // 3. The Safety Net
    @KafkaListener(topics = "reservation-events.DLT", groupId = "reservation-dlt-group")
    public void consumeDeadletterEvent(ReservationEvent event){
        log.warn("Alert: Processing dead-lettered message from reservation-events.DLT for reservation ID: {}", event.getReservationId());
    }
}