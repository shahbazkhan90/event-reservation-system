package com.project.eventreservation.controller;

import com.project.eventreservation.dto.ReservationEvent;
import com.project.eventreservation.model.DeadLetter;
import com.project.eventreservation.repository.DeadLetterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@RestController
public class AdminDlqController {

    private final static Logger log  = LoggerFactory.getLogger(AdminDlqController.class);

    private final DeadLetterRepository letterRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String ,ReservationEvent> kafkaTemplate;


    public AdminDlqController(DeadLetterRepository repository,ObjectMapper mapper,
                              KafkaTemplate<String,ReservationEvent > template){
        this.letterRepository = repository;
        this.objectMapper = mapper;
        this.kafkaTemplate = template;
    }

    @PostMapping("/api/dlq/{id}")
    public ResponseEntity<String> replayDeadletter(@PathVariable Long id){
        DeadLetter letter = letterRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("No dead letter for Id:"+id));

        try {
        ReservationEvent event = objectMapper.readValue(letter.getPayload(),ReservationEvent.class);
        kafkaTemplate.send("reservation-events",event);
        log.info("Admin Action: Replayed dead letter ID {} to reservation-events topic", id);

        letterRepository.delete(letter);
        log.info("Admin Action: Deleted dead letter ID {} from database", id);

        return ResponseEntity.ok("Successfully replayed and purged Dead Letter ID: " + id);
        }
        catch (Exception e) {
            log.error("Catastrophic failure while replaying Dead Letter ID: {}", id, e);
            return ResponseEntity.internalServerError().body("Failed to replay message: " + e.getMessage());
        }
    }

}
