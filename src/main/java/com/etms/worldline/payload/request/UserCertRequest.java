package com.etms.worldline.payload.request;

import lombok.Data;

@Data
public class UserCertRequest {
    private Long user_id;
    private String cert_url;
    private String cert_name;
}
