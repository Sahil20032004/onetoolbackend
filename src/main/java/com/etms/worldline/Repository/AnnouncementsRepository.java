package com.etms.worldline.Repository;

import com.etms.worldline.model.Announcements;
import com.etms.worldline.payload.response.AnnouncementsResponse;
import com.etms.worldline.payload.response.SosQuestionsResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementsRepository extends JpaRepository<Announcements,Long> {

    @Query("SELECT new com.etms.worldline.payload.response.AnnouncementsResponse(s.an_id,s.location) FROM announcements s WHERE s.location=:location AND s.active=true")
    List<AnnouncementsResponse> getAnnouncements(@Param("location") String location);
}
