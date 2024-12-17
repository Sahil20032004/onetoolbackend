package com.etms.worldline.payload.request;

import lombok.Data;

import java.util.List;

@Data
public class SOSsaveResponses {
   private Long user_id;
   private List<QuestionwithAnswer> questionwithAnswer;
}
