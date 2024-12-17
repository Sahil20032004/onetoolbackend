package com.etms.worldline.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "sos_responses")
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class SOSResponses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sos_resp_id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonManagedReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "ques_id")
    @JsonManagedReference
    private SOSQuestions sosQuestions;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private String answer;

    public SOSResponses(User user, SOSQuestions sosQuestions, String answer,LocalDateTime modifiedAt) {
        this.user = user;
        this.sosQuestions = sosQuestions;
        this.answer = answer;
        this.modifiedAt=modifiedAt;
    }
    @PrePersist
    protected void OnCreate(){
        createdAt= LocalDateTime.now();
    }

}
