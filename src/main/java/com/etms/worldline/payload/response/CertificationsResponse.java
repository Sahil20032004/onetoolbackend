package com.etms.worldline.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CertificationsResponse {
  private String cert_name;
  private Long user_count;
}
