package com.etms.worldline.Service;

import com.etms.worldline.Repository.ArchivedTrainingsRepository;
import com.etms.worldline.Repository.TrainingsRepository;
import com.etms.worldline.model.ArchivedTrainings;
import com.etms.worldline.model.Trainings;
import com.etms.worldline.payload.response.TrainingsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrainingArchivalService {

    @Autowired
    private TrainingsRepository trainingsRepository;

    @Autowired
    private ArchivedTrainingsRepository archivedTrainingsRepository;

    public void archiveOldTrainings() {
        LocalDate currentDate = LocalDate.now();
        LocalDate oneMonthAgo = currentDate.minusMonths(1);
        List<TrainingsResponse> completedTrainings = trainingsRepository.findTrainingsOlderThanOneMonth(oneMonthAgo);
        System.out.println("COMPLETED TRAININGS LIST ----- "+completedTrainings);

        for (TrainingsResponse training : completedTrainings) {
            ArchivedTrainings archivedTraining = new ArchivedTrainings();
            archivedTraining.setId(training.getId());
            archivedTraining.setTraining_name(training.getTraining_name());
            archivedTraining.setTraining_date(training.getTraining_date());
            archivedTraining.setSlot_from(training.getSlot_from());
            archivedTraining.setSlot_to(training.getSlot_to());
            archivedTraining.setTrainer_id(training.getTrainer_user_id());
            archivedTraining.setTopics(training.getTopics());
            archivedTraining.setArchivedAt(LocalDateTime.now());
            archivedTraining.setCreated_at(training.getCreated_at());
            archivedTraining.setModified_at(training.getModified_at());

            archivedTrainingsRepository.save(archivedTraining);

            trainingsRepository.deleteById(training.getId());
        }
    }
}
