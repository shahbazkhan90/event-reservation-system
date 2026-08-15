package com.project.eventreservation.repository;

import com.project.eventreservation.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent,Long> {
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(String status);
}
