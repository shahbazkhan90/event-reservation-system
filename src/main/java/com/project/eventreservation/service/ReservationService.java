package com.project.eventreservation.service;

import com.project.eventreservation.dto.PaymentWebhookdto;
import com.project.eventreservation.dto.ReservationEvent;
import com.project.eventreservation.dto.ReservationRequestDTO;
import com.project.eventreservation.exception.BadRequestException;
import com.project.eventreservation.model.Event;
import com.project.eventreservation.model.OutboxEvent;
import com.project.eventreservation.model.Reservation;
import com.project.eventreservation.repository.EventRepository;
import com.project.eventreservation.repository.OutboxEventRepository;
import com.project.eventreservation.repository.ReservationRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
//@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository repository;
//    private final ReservationProducer producer;
    private final EventRepository eventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public ReservationService(EventRepository eventRepository,
                              ReservationRepository reservationRepository,
                              OutboxEventRepository outboxEventRepository,
                              ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.repository = reservationRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }


    @Transactional
    public Reservation createReservation(Long userId,Long eventId,Integer seatsBooked){

        Event event = eventRepository.findByIdWithLock(eventId)
                .orElseThrow(() -> new BadRequestException("Event not found"));

        Integer currentSeats = repository.sumSeatBookedByEventIdAndStatuses(eventId, List.of("CONFIRMED","PENDING"));
        int remainingSeats =event.getTotalCapacity()-currentSeats;
        String initialStatus;
        if(seatsBooked>remainingSeats){
            initialStatus = "WAITLISTED";
        }
        else {
            initialStatus = "PENDING";
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setEventId(eventId);
        reservation.setSeatsBooked(seatsBooked);
        reservation.setStatus(initialStatus);
        Reservation savedReservation = repository.save(reservation);

        try {
            ReservationEvent eventpayload = new ReservationEvent(savedReservation);
            String jsonPayload = objectMapper.writeValueAsString(eventpayload);

            OutboxEvent outboxEvent = new OutboxEvent(
                    savedReservation.getId().toString(),
                    "RESERVATION "+ initialStatus,
                    jsonPayload);

            outboxEventRepository.save(outboxEvent);
        }
        catch (Exception e){
            throw new RuntimeException("Failed to Serialize the event payload",e);
        }

        return savedReservation;


    }

    @Transactional
    public void promoteSeats(Long eventId){
        Event event = eventRepository.findByIdWithLock(eventId)
                .orElseThrow(()-> new RuntimeException("Event not found"));

        int currentSeats = repository.sumSeatBookedByEventIdAndStatuses(event.getId(),List.of("CONFIRMED","PENDING"));
        int remainingSeats = event.getTotalCapacity()-currentSeats;
        if(remainingSeats<=0){
            return;
        }

        Optional<Reservation> oldestReservation = repository.findOldestWaitlistedByEvent(eventId);

        if(oldestReservation.isPresent()){
            Reservation waitlisted = oldestReservation.get();

            if(waitlisted.getSeatsBooked()<=remainingSeats){
                waitlisted.setStatus("PENDING");
                waitlisted.setCreatedAt(LocalDateTime.now());
                repository.save(waitlisted);

                try {
                    OutboxEvent outboxEvent = new OutboxEvent(
                            waitlisted.getId().toString(),
                            "RESERVATION_PROMOTED",
                            objectMapper.writeValueAsString(waitlisted)
                    );
                    outboxEventRepository.save(outboxEvent);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to serialize promotion event", e);
                }

            }
        }
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        // 1. Lock the row to block the payment webhook from interfering
        Reservation reservation = repository.findByIdWithLock(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // 2. Idempotency Check: Only cancel if it's still PENDING
        if (!"PENDING".equals(reservation.getStatus())) {
            return;
        }

        // 3. Apply state transition
        reservation.setStatus("CANCELLED");
        repository.save(reservation);

        // 4. Atomic Outbox write
        try {
            OutboxEvent outboxEvent = new OutboxEvent(
                    reservation.getId().toString(),
                    "RESERVATION_CANCELLED",
                    objectMapper.writeValueAsString(reservation)
            );
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize cancel event payload", e);
        }
    }


    @Transactional
    public void processPayment(PaymentWebhookdto dto ){
        Reservation reservation = repository.findByIdWithLock(dto.getReservationId())
                .orElseThrow(() -> new BadRequestException("Reservation not Found"));

        if(!"PENDING".equals(reservation.getStatus())){
            return;
        }

        if("SUCCESS".equals(dto.getStatus())){
            reservation.setStatus("CONFIRMED");
        } else {
            reservation.setStatus("CANCELLED");
        }

        repository.save(reservation);

        try {
            ReservationEvent payload = new ReservationEvent(reservation);
            String jsonPayload = objectMapper.writeValueAsString(payload);

            String eventType = "SUCCESS".equalsIgnoreCase(dto.getStatus())
                    ? "RESERVATION_CONFIRMED"
                    : "RESERVATION_CANCELLED";

            OutboxEvent outboxEvent = new OutboxEvent(
                    reservation.getId().toString(),
                    eventType,
                    jsonPayload);

            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize the event payload", e);
        }
    }
}
