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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

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
    public Reservation createReservation(ReservationRequestDTO dto){

        Event event = eventRepository.findByIdWithLock(dto.getEventId())
                .orElseThrow(() -> new BadRequestException("Event not found"));

        Integer currentSeats = repository.sumSeatBookedByEventIdAndStatuses(dto.getEventId(), List.of("CONFIRMED","PENDING"));
        int remainingSeats =event.getTotalCapacity()-currentSeats;
        if(dto.getSeatsBooked()>remainingSeats){
            throw new BadRequestException("Not enough seats available. Only "+remainingSeats+" are available");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(dto.getUserId());
        reservation.setEventId(dto.getEventId());
        reservation.setSeatsBooked(dto.getSeatsBooked());
        reservation.setStatus("PENDING");
        Reservation savedReservation = repository.save(reservation);

        try {
            ReservationEvent eventpayload = new ReservationEvent(savedReservation);
            String jsonPayload = objectMapper.writeValueAsString(eventpayload);

            OutboxEvent outboxEvent = new OutboxEvent(
                    savedReservation.getId().toString(),
                    "RESERVATION PENDING",
                    jsonPayload);

            outboxEventRepository.save(outboxEvent);
        }
        catch (Exception e){
            throw new RuntimeException("Failed to Serialize the event payload",e);
        }

        return savedReservation;


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
        }
        else {
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
            throw new RuntimeException("Failed to Serialize the event payload", e);
        }
    }

}
