package com.project.eventreservation.controller;


import com.project.eventreservation.dto.ReservationRequestDTO;
import com.project.eventreservation.model.Reservation;
import com.project.eventreservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;

    @PostMapping("")
    public ResponseEntity<Reservation> createReservation(@Valid @RequestBody ReservationRequestDTO dto){
        return new ResponseEntity<>(service.createReservation(dto), HttpStatus.CREATED);
    }


}
