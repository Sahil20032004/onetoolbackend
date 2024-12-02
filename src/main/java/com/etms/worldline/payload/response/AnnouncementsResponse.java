package com.etms.worldline.payload.response;

import lombok.Data;

import java.util.List;

@Data
public class AnnouncementsResponse {
    private Long an_id;
    private List<String> announcements;
    private String location;

    public AnnouncementsResponse(Long an_id,String location) {
        this.an_id = an_id;
        this.location=location;
    }
}
