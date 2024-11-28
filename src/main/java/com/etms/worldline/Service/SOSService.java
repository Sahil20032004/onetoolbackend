package com.etms.worldline.Service;

import com.etms.worldline.Repository.AnnouncementsRepository;
import com.etms.worldline.Repository.SOSQuestionsRepository;
import com.etms.worldline.Repository.SOSResponsesRepository;
import com.etms.worldline.Repository.UserRepository;
import com.etms.worldline.model.Announcements;
import com.etms.worldline.model.SOSResponses;
import com.etms.worldline.model.User;
import com.etms.worldline.payload.request.QuestionwithAnswer;
import com.etms.worldline.payload.request.SOSsaveResponses;
import com.etms.worldline.payload.response.*;
import com.etms.worldline.model.SOSQuestions;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class SOSService {

    @Autowired
    private SOSQuestionsRepository sosQuestionsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SOSResponsesRepository sosResponsesRepository;
    @Autowired
    private AnnouncementsRepository announcementsRepository;

    public ResponseEntity<?> saveSosQuestions(List<SOSQuestions> sosQuestions){
        try {
            sosQuestionsRepository.saveAll(sosQuestions);
            return ResponseEntity.ok(new MessageResponse(20001,"SosQuestions Saved Successfully"));
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new MessageResponse(50000,"Internal Server error"));
        }
            }
            public ResponseEntity<?> getSocQuestions(String location){
             try{
                 List<SosQuestionsResponse> sosQuestions=sosQuestionsRepository.getQuestions(location);
                 for(SosQuestionsResponse questions:sosQuestions){
                     questions.setChoices(sosQuestionsRepository.findById(questions.getQues_id()).get().getChoices());
                 }
                 return ResponseEntity.ok(new SOSQResponses(20001,"Fetched records Successfully",sosQuestions));
             }
             catch (Exception e){
                 e.printStackTrace();
                 return ResponseEntity.ok(new MessageResponse(50000,"Internal Server error"));
             }
            }
         public ResponseEntity<?> saveResponses(SOSsaveResponses soSsaveResponses){
           try{
               System.out.println(soSsaveResponses.getUser_id());
               User user=userRepository.findById(soSsaveResponses.getUser_id()).get();
               for(QuestionwithAnswer questionwithAnswer:soSsaveResponses.getQuestionwithAnswer()){
                   Optional<SOSQuestions> questions=sosQuestionsRepository.findById(questionwithAnswer.getQues_id());
                   Optional<SOSResponses> responses=sosResponsesRepository.findByUserandQuesId(user.getUser_id(),questionwithAnswer.getQues_id());
                   if(responses.isPresent()){
                       Long daysBetween= ChronoUnit.DAYS.between(responses.get().getModifiedAt(),LocalDateTime.now());
                       if(daysBetween<=1){
                           return ResponseEntity.ok(new MessageResponse(40001,"Your Responses are already submitted! Try doing it tomorrow"));
                       }
                   }
                   sosResponsesRepository.save(new SOSResponses(user,questions.get(),questionwithAnswer.getAnswer(), LocalDateTime.now()));
               }
               return ResponseEntity.ok(new MessageResponse(20001,"Responses saved Successfully"));
           }
           catch (Exception e){
               e.printStackTrace();
               return ResponseEntity.internalServerError().body(new MessageResponse(50000,"Internal Server error"));
           }
         }
    public ResponseEntity<?> saveAnnouncements(List<Announcements> sosAnnouncements){
        try {
            announcementsRepository.saveAll(sosAnnouncements);
            return ResponseEntity.ok(new MessageResponse(20001,"Announcements Saved Successfully"));
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new MessageResponse(50000,"Internal Server error"));
        }
    }
    public ResponseEntity<?> getAnnouncements(String location){
        try{
            List<AnnouncementsResponse> announcements=announcementsRepository.getAnnouncements(location);
            for(AnnouncementsResponse announcement:announcements){
                announcement.setAnnouncements(announcementsRepository.findById(announcement.getAn_id()).get().getAnnouncements());
            }
            return ResponseEntity.ok(new FetchAnnouncementsResponse(20001,"Fetched records Successfully",announcements));
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok(new MessageResponse(50000,"Internal Server error"));
        }
    }
}
