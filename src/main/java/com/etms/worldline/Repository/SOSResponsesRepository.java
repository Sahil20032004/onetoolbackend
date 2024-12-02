package com.etms.worldline.Repository;

import com.etms.worldline.model.SOSResponses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SOSResponsesRepository extends JpaRepository<SOSResponses,Long> {
    @Query("SELECT s FROM sos_responses s WHERE s.user.user_id=:user_id AND s.sosQuestions.ques_id=:ques_id")
    Optional<SOSResponses> findByUserandQuesId(@Param("user_id") Long user_id,@Param("ques_id") Long ques_id);
}
