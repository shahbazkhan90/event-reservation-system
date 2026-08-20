package com.project.eventreservation.service;

import com.project.eventreservation.model.OutboxEvent;
import com.project.eventreservation.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OutRelayService {
    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String ,String  > kafkaTemplate;

    public OutRelayService(OutboxEventRepository outboxRepository,@Qualifier("stringKafkaTemplate") KafkaTemplate<String,String> kafkaTemplate){
        this.outboxRepository= outboxRepository;
        this.kafkaTemplate= kafkaTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void processPendingEvents(){
        List<OutboxEvent> events = outboxRepository.findPendingEventsForProcessing();
        if(events.isEmpty()){
            return;
        }
        for(OutboxEvent event:events){
            try {
                kafkaTemplate.send("reservation-events",event.getAggregateId(),event.getPayload());
                event.setStatus("PROCESSED");
            }
            catch (Exception e){
                event.setStatus("FAILED");
            }
        }

        outboxRepository.saveAll(events);
    }
}
