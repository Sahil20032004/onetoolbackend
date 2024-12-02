package com.etms.worldline.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@RequiredArgsConstructor
public class ArchivedSkillHub {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long skillSetId;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    @PrePersist
    protected void OnCreate(){
        createdAt= LocalDateTime.now();
    }
}
