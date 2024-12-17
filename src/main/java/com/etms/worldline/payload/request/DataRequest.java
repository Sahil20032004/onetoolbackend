package com.etms.worldline.payload.request;

import lombok.Data;

@Data
public class DataRequest {

    private Long user_id;

    private String title;

    private String body;

    private String action;
}
