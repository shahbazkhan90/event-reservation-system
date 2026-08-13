package com.project.eventreservation.service;

import com.project.eventreservation.dto.ReservationEvent;
import com.project.eventreservation.dto.ReservationRequestDTO;
import com.project.eventreservation.exception.BadRequestException;
import com.project.eventreservation.kafka.ReservationProducer;
import com.project.eventreservation.model.Event;
import com.project.eventreservation.model.Reservation;
import com.project.eventreservation.repository.EventRepository;
import com.project.eventreservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.id.IntegralDataTypeHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository repository;
    private final ReservationProducer producer;
    private final EventRepository eventRepository;


    @Transactional
    public Reservation createReservation(ReservationRequestDTO dto){
        Event event = eventRepository.findByIdWithLock(dto.getEventId())
                .orElseThrow(() -> new BadRequestException("Event not found"));
        Integer currentSeats = repository.sumSeatBookedByEventIdAndStatus(dto.getEventId(),"CONFIRMED");
        int remainingSeats =event.getTotalCapacity()-currentSeats;
        if(dto.getSeatsBooked()>remainingSeats){
            throw new BadRequestException("Not enough seats available. Only "+remainingSeats+" are available");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(dto.getUserId());
        reservation.setEventId(dto.getEventId());
        reservation.setSeatsBooked(dto.getSeatsBooked());
        reservation.setStatus("CONFIRMED");

        Reservation savedReservation = repository.save(reservation);

        ReservationEvent events = new ReservationEvent(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getEventId(),
                reservation.getSeatsBooked(),
                reservation.getStatus());

        producer.sendReservationEvent(events);

        return savedReservation;





    }

}
