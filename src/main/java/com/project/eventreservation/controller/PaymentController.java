package com.project.eventreservation.controller;

import com.project.eventreservation.dto.PaymentWebhookdto;
import com.project.eventreservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/payments")
public class PaymentController {

    private final ReservationService reservationService;

    public PaymentController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@Valid @RequestBody PaymentWebhookdto dto) {
        reservationService.processPayment(dto);
        return ResponseEntity.ok().build();




    }
}
