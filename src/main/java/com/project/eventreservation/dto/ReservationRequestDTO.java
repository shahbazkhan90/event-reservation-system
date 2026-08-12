package com.project.eventreservation.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDTO {
    @NotNull
    private Long userId;
    @NotNull
    private Long eventId;
    @NotNull
    @Min(value = 1,message = "Must book at least 1 ticket")
    private Integer seatsBooked;
}
