package com.project.eventreservation.kafka;

import com.project.eventreservation.dto.ReservationEvent;
import com.project.eventreservation.model.DeadLetter;
import com.project.eventreservation.model.ProcessedMessage;
import com.project.eventreservation.repository.DeadLetterRepository;
import com.project.eventreservation.repository.ProcessedMessageRepository;
import com.project.eventreservation.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
public class ReservationConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReservationConsumer.class);
    private final NotificationService notificationService;
    private final ProcessedMessageRepository messageRepository;
    private final DeadLetterRepository letterRepository;
    private final ObjectMapper objectMapper;

    public ReservationConsumer(NotificationService notificationService,ProcessedMessageRepository repository,
                                DeadLetterRepository letterRepository,ObjectMapper mapper) {
        this.notificationService = notificationService;
        this.messageRepository =repository;
        this.letterRepository=letterRepository;
        this.objectMapper = mapper;
    }

    @KafkaListener(topics = "reservation-events" , groupId = "reservation-group-v10")
    public void consumeReservationEvent(ReservationEvent event){
        String idempotencykey = "RES-"+event.getReservationId()+"-"+event.getStatus();
        try {
            messageRepository.saveAndFlush(new ProcessedMessage(idempotencykey, LocalDateTime.now()));
        }
        catch (DataIntegrityViolationException e){
            log.warn("[IDEMPOTENCY ALERT] Duplicate message detected and dropped for key: {}", idempotencykey);
            return;
        }
        log.info("[KAFKA CONSUMER] Processing Event for Reservation ID: {} | Status: {}", event.getReservationId(), event.getStatus());

        if(event.getUserId() != null && event.getUserId().equals(999L)){
            log.error("Simulating downstream failure for userId: 999. Crashing consumer...");
            throw new RuntimeException("Simulated downstream processing failure for userId: " + event.getUserId());
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
        log.error("CRITICAL : Message permanently failed for Reservation Id:{}. Routing to Database Morgue",event.getReservationId());
        try{
            String rawPayload = objectMapper.writeValueAsString(event);
            DeadLetter letter = new DeadLetter(event.getReservationId(),rawPayload);
            letterRepository.save(letter);
            log.info("Successfully secured dead letter in the database for manual triage.");
        }
        catch (Exception e){
            log.error("CATASTROPHIC FAILURE: Could not save dead letter to database. Payload lost for Reservation ID: {}", event.getReservationId(), e);
        }
    }
}