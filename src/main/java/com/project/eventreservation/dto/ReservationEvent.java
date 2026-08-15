package com.project.eventreservation.dto;

import com.project.eventreservation.model.Reservation;
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

    public ReservationEvent(Reservation savedReservation) {
        this.reservationId=savedReservation.getId();
        this.userId= savedReservation.getUserId();
        this.eventId=savedReservation.getEventId();
        this.seatsBooked=savedReservation.getSeatsBooked();
        this.status=savedReservation.getStatus();
    }
}
