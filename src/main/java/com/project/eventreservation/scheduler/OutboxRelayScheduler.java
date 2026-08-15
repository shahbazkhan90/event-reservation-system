package com.project.eventreservation.scheduler;

import com.project.eventreservation.dto.ReservationEvent;
import com.project.eventreservation.model.OutboxEvent;
import com.project.eventreservation.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class OutboxRelayScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayScheduler.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, ReservationEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxRelayScheduler(OutboxEventRepository outboxEventRepository,
                                KafkaTemplate<String, ReservationEvent> kafkaTemplate,
                                ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void processOutboxEvents(){
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING");

        for(OutboxEvent events : pendingEvents){
            try {
                ReservationEvent payload = objectMapper.readValue(events.getPayload(),ReservationEvent.class);

                kafkaTemplate.send("reservation-events",payload).get();

                events.setStatus("PROCESSED");
                outboxEventRepository.save(events);

                log.info("Successfully relayed Outbox Event ID {} to Kafka",events.getId());
            }
            catch (Exception e){
                log.error("Failed to relay Outbox Event ID {}",events.getId(), e);
            }
        }
    }

}
