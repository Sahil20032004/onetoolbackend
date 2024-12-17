package com.etms.worldline.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SosQuestionsResponse {
  private Long ques_id;
  private String question;
  private List<String> choices;
  private String type;

  public SosQuestionsResponse(Long ques_id, String question, String type) {
    this.ques_id = ques_id;
    this.question = question;
    this.type = type;
  }
}
