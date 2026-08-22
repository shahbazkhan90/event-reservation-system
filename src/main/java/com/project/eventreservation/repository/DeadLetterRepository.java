package com.project.eventreservation.repository;

import com.project.eventreservation.model.DeadLetter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadLetterRepository extends JpaRepository<DeadLetter,Long> {
}
