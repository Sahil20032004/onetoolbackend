package com.etms.worldline.model;

import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@RequiredArgsConstructor
public class ArchivedTrainings {
    @Id
    private Long id;

    @Column(name = "training_name", length = 150, nullable = false)
    private String training_name;

    @Column(name = "training_date", nullable = false)
    private LocalDate training_date;

    @Column(name = "slot_from", nullable = false)
    private LocalDateTime slot_from;

    @Column(name = "slot_to", nullable = false)
    private LocalDateTime slot_to;

    @Column(name = "trainer_id")
    private Long trainer_id;

    private LocalDateTime archivedAt;

    private LocalDateTime created_at;

    private LocalDateTime modified_at;

    @ElementCollection
    @CollectionTable(name = "training_topics", joinColumns = @JoinColumn(name = "training_id"))
    @Column(name = "topic", length = 50, nullable = false)
    private List<String> topics;
}