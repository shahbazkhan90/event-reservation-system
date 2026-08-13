package com.project.eventreservation.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "events")
@Data
public class Event {

    @Id
    private Long id;

    private String name;

    private Integer totalCapacity;
}