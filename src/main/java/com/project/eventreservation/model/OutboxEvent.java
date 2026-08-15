package com.project.eventreservation.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Data
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String aggregateId;
    @Column(nullable = false)
    private String eventType;
    @Column(nullable = false,columnDefinition = "TEXT")
    private String payload;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public OutboxEvent(String aggregateId,String eventType,String payload){
        this.aggregateId=aggregateId;
        this.eventType=eventType;
        this.payload=payload;
        this.status="PENDING";
    }

}
