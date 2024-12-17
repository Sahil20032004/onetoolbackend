package com.etms.worldline.payload.request;

import lombok.Data;

@Data
public class Message {
    private String token;
    private Notification notification;
    private DataRequest data;
}
