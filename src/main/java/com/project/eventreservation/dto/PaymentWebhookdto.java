package com.project.eventreservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentWebhookdto {
    @NotNull(message = "Reservation ID is required ")
    private Long reservationId;
    @NotNull(message = "Status is required ")
    private String status;
    @NotNull(message = "Transaction ID is required ")
    private String transactionId;
}
