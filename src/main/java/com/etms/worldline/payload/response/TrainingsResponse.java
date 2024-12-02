package com.etms.worldline.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TrainingsResponse {
    private Long id;

    private String training_name;

    private LocalDate training_date;

    private LocalDateTime slot_from;

    private LocalDateTime slot_to;

    private LocalDateTime created_at;

    private LocalDateTime modified_at;

    private String trainer;

    private Long trainer_user_id;

    private List<String> topics;

    public TrainingsResponse(Long id, String trainingName, LocalDate trainingDate, LocalDateTime slotFrom, LocalDateTime slotTo,Long trainer_user_id, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.training_name = trainingName;
        this.training_date = trainingDate;
        this.slot_from = slotFrom;
        this.slot_to = slotTo;
        this.trainer_user_id=trainer_user_id;
        this.created_at=createdAt;
        this.modified_at=modifiedAt;
    }

    public TrainingsResponse(Long id, String trainingName, LocalDate trainingDate, LocalDateTime slotFrom, LocalDateTime slotTo, String trainer, Long trainer_user_id) {
        this.id = id;
        this.training_name = trainingName;
        this.training_date = trainingDate;
        this.slot_from = slotFrom;
        this.slot_to = slotTo;
        this.trainer = trainer;
        this.trainer_user_id=trainer_user_id;
    }
    public TrainingsResponse(Long id, String trainingName, LocalDate trainingDate, LocalDateTime slotFrom, LocalDateTime slotTo, String trainer) {
        this.id = id;
        this.training_name = trainingName;
        this.training_date = trainingDate;
        this.slot_from = slotFrom;
        this.slot_to = slotTo;
        this.trainer = trainer;
    }
    public TrainingsResponse(){}

}
