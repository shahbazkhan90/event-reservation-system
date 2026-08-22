package com.project.eventreservation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "processed_messages")
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedMessage {
    @Id
    @Column(nullable = false,name = "message_id",updatable = false)
    private String messageId;
    @Column(nullable = false,updatable = false)
    private LocalDateTime processedAt;
}
