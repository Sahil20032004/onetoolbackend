package com.etms.worldline.scheduler;

import com.etms.worldline.Service.SkillHubArchivalService;
import com.etms.worldline.Service.TrainingArchivalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ArchivalScheduler {

    @Autowired
    private TrainingArchivalService trainingArchivalService;

    @Autowired
    private SkillHubArchivalService skillHubArchivalService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleTrainingArchival() {
        log.info("Starting scheduled archival job");
        try {
            trainingArchivalService.archiveOldTrainings();
            log.info("Completed scheduled archival job");
        } catch (Exception e) {
            log.error("Scheduled archival job failed: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 59 23 L * ?")
    public void scheduleSkillHubArchival() {
        log.info("Starting scheduled archival job for SkillHub");
        try {
            skillHubArchivalService.archiveOldSkillHubRequests();
            log.info("Completed scheduled archival job");
        } catch (Exception e) {
            log.error("Scheduled archival job failed: {}", e.getMessage());
        }
    }
}
