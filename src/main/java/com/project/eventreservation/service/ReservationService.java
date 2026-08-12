package com.project.eventreservation.service;

import com.project.eventreservation.dto.ReservationRequestDTO;
import com.project.eventreservation.model.Reservation;
import com.project.eventreservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository repository;

//    public ReservationService(ReservationRepository repository){
//        this.repository= repository;
//    }

    public Reservation createReservation(ReservationRequestDTO dto){
        Reservation reservation = new Reservation();
        reservation.setUserId(dto.getUserId());
        reservation.setEventId(dto.getEventId());
        reservation.setSeatsBooked(dto.getSeatsBooked());
        reservation.setStatus("CONFIRMED");
        return repository.save(reservation);


    }

}
