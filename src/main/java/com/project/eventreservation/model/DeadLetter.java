package com.project.eventreservation.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "dead_letters")
@NoArgsConstructor
public class DeadLetter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long reservationId;
    @Column(nullable = false,columnDefinition = "TEXT")
    private String payload;
    @Column(nullable = false,updatable = false)
    private LocalDateTime failedAt = LocalDateTime.now();

    public DeadLetter(Long reservationId, String payload) {
        this.reservationId = reservationId;
        this.payload = payload;
    }
}
