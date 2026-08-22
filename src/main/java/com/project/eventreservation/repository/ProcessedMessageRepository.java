package com.project.eventreservation.repository;

import com.project.eventreservation.model.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage,String> {
}
