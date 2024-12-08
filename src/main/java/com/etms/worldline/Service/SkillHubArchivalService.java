package com.etms.worldline.Service;

import com.etms.worldline.Repository.ArchivedSkillHubRepository;
import com.etms.worldline.Repository.SkillHubRepository;
import com.etms.worldline.Repository.ArchivedTrainingsRepository;
import com.etms.worldline.model.ArchivedSkillHub;
import com.etms.worldline.payload.response.SkillHubResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SkillHubArchivalService {

    @Autowired
    private SkillHubRepository skillHubRepository;

    @Autowired
    private ArchivedSkillHubRepository archivedSkillHubRepository;

    public void archiveOldSkillHubRequests() {
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime oneMonthAgo = currentDate.minusMonths(1);
        List<SkillHubResponse> oldSkillHubRequests = skillHubRepository.findSkillHubRequestsOlderThanOneMonth(oneMonthAgo);
        System.out.println("OLD SKILL HUB REQUESTS LIST ----- "+oldSkillHubRequests);

        for (SkillHubResponse skillHubRequest : oldSkillHubRequests) {
            ArchivedSkillHub archivedSkillHub = new ArchivedSkillHub();
            archivedSkillHub.setId(skillHubRequest.getId());
            archivedSkillHub.setUserId(skillHubRequest.getUser_id());
            archivedSkillHub.setSkillSetId(skillHubRequest.getSkill_set_id());
            archivedSkillHub.setCreatedAt(skillHubRequest.getCreated_at());
            archivedSkillHub.setModifiedAt(skillHubRequest.getModified_at());

            archivedSkillHubRepository.save(archivedSkillHub);

            skillHubRepository.deleteById(skillHubRequest.getId());
        }
    }
}
