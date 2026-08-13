package com.project.eventreservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEvent {
    private Long reservationId;
    private Long userId;
    private Long eventId;
    private Integer seatsBooked;
    private String status;
}
