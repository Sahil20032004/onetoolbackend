package com.etms.worldline.payload.request;

import lombok.Data;

@Data
public class PassowordResetRequest {
   private String username;
   private String new_password;
   private String confirm_password;
   private String old_password;
}
