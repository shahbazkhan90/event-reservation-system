package com.project.eventreservation.scheduler;

import ch.qos.logback.core.util.FixedDelay;
import com.project.eventreservation.dto.ReservationEvent;
import com.project.eventreservation.model.OutboxEvent;
import com.project.eventreservation.model.Reservation;
import com.project.eventreservation.repository.OutboxEventRepository;
import com.project.eventreservation.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PaymentTTLExecutor {
    private static final Logger logger = LoggerFactory.getLogger(PaymentTTLExecutor.class);

    private final ReservationRepository repository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public PaymentTTLExecutor(ReservationRepository reservationRepository,
                              OutboxEventRepository outboxEventRepository,
                              ObjectMapper objectMapper){
        this.repository = reservationRepository;
        this.outboxRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void releaseUnpaidSeats(){
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);

        List<Reservation> expiredReservations = repository.findExpiredPendingReservations(cutoff);

        if(expiredReservations.isEmpty()){
            return;
        }

        for (Reservation reservation : expiredReservations){
            reservation.setStatus("CANCELLED");
            repository.save(reservation);

            try {
                ReservationEvent reservationEvent = new ReservationEvent(reservation);
                String jsonPayload = objectMapper.writeValueAsString(reservationEvent);
                OutboxEvent outboxEvent = new OutboxEvent(
                        reservation.getId().toString(),
                        "RESERVATION_CANCELLED",
                        jsonPayload);
                outboxRepository.save(outboxEvent);
                logger.info("Released unpaid reservation ID {} and restored seat capacity.", reservation.getId());
            }
            catch (Exception e){
                logger.error("Failed to process compensation for reservation ID {}", reservation.getId(), e);
            }
        }
    }
}
