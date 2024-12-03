package com.etms.worldline.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FetchUserCertificates {
  private String cert_name;
  private String cert_url;
  private String cert_path;
}
