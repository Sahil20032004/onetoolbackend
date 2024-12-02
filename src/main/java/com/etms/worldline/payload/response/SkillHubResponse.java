package com.etms.worldline.payload.response;

import com.etms.worldline.model.SkillSet;
import com.etms.worldline.model.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SkillHubResponse {
    private Long id;
    private Long user_id;
    private Long skill_set_id;
    private LocalDateTime created_at;
    private LocalDateTime modified_at;

    public SkillHubResponse(Long id, Long userId, Long skillSetId, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.user_id = userId;
        this.skill_set_id = skillSetId;
        this.created_at = createdAt;
        this.modified_at = modifiedAt;
    }
}
