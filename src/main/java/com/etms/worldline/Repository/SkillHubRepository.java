package com.etms.worldline.Repository;

import com.etms.worldline.model.SkillHub;
import com.etms.worldline.payload.response.SkillHubResponse;
import com.etms.worldline.payload.response.TrainingsResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SkillHubRepository extends JpaRepository<SkillHub,Long> {
    @Query("SELECT new com.etms.worldline.payload.response.SkillHubResponse(s.id, s.user.user_id, s.skillSet.id, s.createdAt, s.modifiedAt) " +
            "FROM SkillHub s " +
            "WHERE s.createdAt <= :oneMonthAgo " +
            "ORDER BY s.createdAt ASC")
    List<SkillHubResponse> findSkillHubRequestsOlderThanOneMonth(@Param("oneMonthAgo") LocalDateTime oneMonthAgo);
    void deleteById(Long id);

    @Query("SELECT s.skillSet.skillName FROM SkillHub s WHERE s.admin_approval='approved' AND s.user.user_id=:user_id")
    List<String> getApprovedSkillHubSkills(@Param("user_id") Long user_id);
}
