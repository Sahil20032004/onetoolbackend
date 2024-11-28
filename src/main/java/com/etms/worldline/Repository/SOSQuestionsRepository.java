package com.etms.worldline.Repository;

import com.etms.worldline.model.SOSQuestions;
import com.etms.worldline.payload.response.SosQuestionsResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SOSQuestionsRepository extends JpaRepository<SOSQuestions,Long> {
    @Query("SELECT new com.etms.worldline.payload.response.SosQuestionsResponse(s.ques_id,s.questions,s.ques_type) FROM sos_questions s WHERE s.location=:location AND s.active=true")
    List<SosQuestionsResponse> getQuestions(@Param("location") String location);
}
