package com.project.eventreservation.repository;

import com.project.eventreservation.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent,Long> {
    @Query(value = """
            SELECT * FROM outbox_events 
            WHERE status = 'PENDING' 
            ORDER BY created_at ASC 
            LIMIT 50 
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> findPendingEventsForProcessing();
}
