package com.etms.worldline.payload.response;

import com.etms.worldline.payload.response.SosQuestionsResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SOSQResponses {
  private int response_code;
  private String message;
  private List<SosQuestionsResponse> responses;
}
