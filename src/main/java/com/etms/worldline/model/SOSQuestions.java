package com.etms.worldline.model;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "sos_questions")
@Data
@RequiredArgsConstructor
public class SOSQuestions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ques_id;

    @Column(length = 150)
    private String questions;

    @ElementCollection
    @CollectionTable(name = "sos_questions_choices", joinColumns = @JoinColumn(name = "ques_id"))
    @Column(name = "choices",length=50)
    private List<String> choices;

    @Column(length = 50)
    private String ques_type;

    @Column(length = 20)
    private String location;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private boolean active=true;

    @PrePersist
    protected void OnCreate(){
        createdAt= LocalDateTime.now();
    }

}
