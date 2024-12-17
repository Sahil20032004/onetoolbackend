package com.etms.worldline.payload.response;

import lombok.Data;

@Data
public class NominatedSkillsStatusResponse {
   private String skill_name;
   private String manager_approval;
   private String admin_approval;
   private String comments;

    public NominatedSkillsStatusResponse(String skill_name, String manager_approval, String admin_approval,String comments) {
        this.skill_name = skill_name;
        this.manager_approval = manager_approval;
        this.admin_approval = admin_approval;
        this.comments=comments;
    }
}
