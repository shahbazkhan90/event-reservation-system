package com.project.eventreservation.service;

import com.project.eventreservation.dto.ReservationRequestDTO;
import com.project.eventreservation.exception.BadRequestException;
import com.project.eventreservation.model.Reservation;
import com.project.eventreservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.id.IntegralDataTypeHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository repository;


    public Reservation createReservation(ReservationRequestDTO dto){
        Integer currentSeats = repository.sumSeatBookedByEventIdAndStatus(dto.getEventId(),"CONFIRMED");
        int maxCapacity = 10;
        int remainingSeats = maxCapacity-currentSeats;
        if(dto.getSeatsBooked()>remainingSeats){
            throw new BadRequestException("Not enough seats available. Only "+remainingSeats+" are available");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(dto.getUserId());
        reservation.setEventId(dto.getEventId());
        reservation.setSeatsBooked(dto.getSeatsBooked());
        reservation.setStatus("CONFIRMED");

        return repository.save(reservation);


    }

}
