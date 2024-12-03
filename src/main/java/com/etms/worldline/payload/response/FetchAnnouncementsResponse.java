package com.etms.worldline.payload.response;

import com.etms.worldline.model.Announcements;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FetchAnnouncementsResponse {
    private int response_code;
    private String message;
    private List<AnnouncementsResponse> responses;
}
