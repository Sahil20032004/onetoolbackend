package com.etms.worldline.Repository;

import com.etms.worldline.model.ArchivedSkillHub;
import com.etms.worldline.model.ArchivedTrainings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchivedSkillHubRepository extends JpaRepository<ArchivedSkillHub, Long> {
}
