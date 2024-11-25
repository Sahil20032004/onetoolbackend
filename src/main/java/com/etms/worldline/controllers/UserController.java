package com.etms.worldline.controllers;

import com.etms.worldline.Repository.TrainerRepository;
import com.etms.worldline.Repository.UserRepository;
import com.etms.worldline.Service.*;
import com.etms.worldline.model.*;
import com.etms.worldline.payload.request.*;
import com.etms.worldline.payload.response.*;
import com.etms.worldline.security.JwtAuthFilter;
import com.etms.worldline.security.JwtTokenProvider;
import org.hibernate.annotations.Fetch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private QuizService quizService;
    @Autowired
    private UserSkillsService userSkillsService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TrainingService trainingService;
    @Autowired
    private TrainerRepository trainerRepository;
    @Autowired
    private JwtAuthFilter jwtAuthFilter;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @GetMapping("/profile")
    public ResponseEntity<?> sample(){
        return ResponseEntity.ok(new MessageResponse(20001,"Hello User!"));
    }
    @GetMapping("getempid")
    public User getempidbyname(@RequestParam @PathVariable String name){
        return userService.getempid(name);
    }
    @GetMapping("getempidbyid")
    public User getempidbyid(@RequestParam @PathVariable Long id){
        return userService.getU(id);
    }

    @PostMapping("/complete")
    public ResponseEntity<?> completeQuiz(@RequestParam @PathVariable Long user_id,@RequestParam @PathVariable Long marks){
        return userService.quizCompletion(user_id);
    }
    @GetMapping("/getQuiz")
    public List<Quiz> getQuiz(@RequestParam @PathVariable Long quiznum){
        return quizService.getQuiz(quiznum);
    }
    @GetMapping("/getquiznos")
    public List<Long> getquiznos(@RequestParam @PathVariable Long userId){
        return quizService.getQuizNo(userId);
    }
    @GetMapping("/getUsersProjects")
    public List<UserProjectDetails> projectDetails(@RequestParam @PathVariable Long id){
        return userService.getTheUsersProjects(id);
    }
    @GetMapping("/getallempskills")
    public List<skilldetails> getallskillsdetails(HttpServletRequest request){
        Long id=jwtTokenProvider.getUserIdFromToken(jwtAuthFilter.getJwtRequest(request));
        return userSkillsService.getallempskills(id);
    }
    @GetMapping("/getemplackingskills")
    public List<lackingskilldetails> getemplackskills(@RequestParam @PathVariable Long id){
        return userSkillsService.getemplackingskills(id);
    }
    @GetMapping("getempskill")
    public List<skilldetails> getempSkills(@RequestParam @PathVariable Long id){
        return userSkillsService.getempskill(id);
    }
    @GetMapping("/skill")
    public ResponseEntity<?> getskill(){
     return userService.fetchSkills();
    }
    @PostMapping("/trainer/nominee")
    public ResponseEntity<?> applyNominations(HttpServletRequest request,@RequestBody nominationsRequest nom){
           nom.setUser_id(jwtTokenProvider.getUserIdFromToken(jwtAuthFilter.getJwtRequest(request)));
           return trainingService.applyNominations(nom);
    }
    @GetMapping("/getAppSkills")
    public List<String> getApprovedSkills(HttpServletRequest request){
        Long user_id=jwtTokenProvider.getUserIdFromToken(jwtAuthFilter.getJwtRequest(request));
        return trainingService.getApprovedSkills(user_id);
    }

    @GetMapping("/getAppSkillHub")
    public List<String> getApprovedSkillHubSkills(HttpServletRequest request){
        Long user_id=jwtTokenProvider.getUserIdFromToken(jwtAuthFilter.getJwtRequest(request));
        return userSkillsService.getApprovedSkillHubSkills(user_id);
    }

    @PutMapping("/nomineeAck")
    public ResponseEntity<?> updateAck(HttpServletRequest request,@RequestBody NomineeReq nomineeReq){
        return trainingService.ackNominationsMan(nomineeReq.getUser_id(),nomineeReq.getSkill_name(),nomineeReq.isAcknowledgement(),nomineeReq.getComments());
    }
    @PutMapping("/nomineeAckAdm")
    public ResponseEntity<?> updateAckAdm(@RequestBody NomineeReq nomineeReq){
        return trainingService.ackNominationsAdm(nomineeReq.getUser_id(),nomineeReq.getSkill_name(),nomineeReq.isAcknowledgement(),nomineeReq.getComments());
    }

    @GetMapping("/getTraining")
    public Trainings getTrainingById(@RequestParam @PathVariable Long id) {
        return trainingService.getTrainingById(id)
                .orElseThrow(() -> new RuntimeException("Training not found with id: " + id));
    }

    @GetMapping("/getAlltrainings")
    public List<Trainings> getAllTrainings() {
        return trainingService.getAllTrainings();
    }

    @PostMapping("/registerTraining")
    public List<Trainings> createTraining(@RequestBody List<TrainingRequest> training) {
        return trainingService.saveTraining(training);
    }
    @PutMapping("/updateTraining")
    public Trainings updateTraining(@RequestBody TrainingRequest trainingRequest){
        return trainingService.updateTraining(trainingRequest);
    }
    @PutMapping("/saveTopicsforTraining")
    public ResponseEntity<?> saveTopics(@RequestBody TrainingRequest trainings){
        return trainingService.saveTopicsforTraining(trainings);
    }
    @GetMapping("/training/history")
    public FetchTrainingsResponse getPastTrainings(HttpServletRequest request,@RequestParam @PathVariable int page,@RequestParam @PathVariable int size){
        Long user_id=jwtTokenProvider.getUserIdFromToken(jwtAuthFilter.getJwtRequest(request));
        System.out.println(page);
        return trainingService.getPastTrainings(user_id,page,size);
    }
    @GetMapping("/trainer/history")
    public FetchTrainerTrainingResponse getPastTrainingsforTrainer(HttpServletRequest request,@RequestParam @PathVariable int page,@RequestParam @PathVariable int size){
            Long user_id=jwtTokenProvider.getUserIdFromToken(jwtAuthFilter.getJwtRequest(request));

            return trainingService.getPastTrainingsforTrainer(user_id,page,size);
    }
    @GetMapping("/training/upcoming")
    public FetchTrainingsResponse getUpcomingTrainings(@RequestParam @PathVariable int page,@RequestParam @PathVariable int size){
        return trainingService.getUpcomingTrainings(page,size);
    }
    @GetMapping("/trainer/upcoming")
    public FetchTrainerTrainingResponse getUpcomingTrainingsfortrainer(HttpServletRequest request,@RequestParam @PathVariable int page,@RequestParam @PathVariable int size){
        Long trainer_id=jwtTokenProvider.getUserIdFromToken(jwtAuthFilter.getJwtRequest(request));
        return trainingService.getUpcomingTrainingsforTrainer(trainer_id,page,size);
    }
    @PostMapping("/training/register")
    public ResponseEntity<?> userReg(HttpServletRequest request,@RequestBody NomineeReq userTrainReg){
        userTrainReg.setUser_id(jwtTokenProvider.getUserIdFromToken(jwtAuthFilter.getJwtRequest(request)));
        System.out.println(userTrainReg.getUser_id());
      return trainingService.userTrainingReg(userTrainReg.getUser_id(),userTrainReg.getTraining_id());
    }
    @GetMapping("/training/status")
    public ResponseEntity<?> getUserTrainingstatus(HttpServletRequest request){
            Long user_id=jwtTokenProvider.getUserIdFromToken(jwtAuthFilter.getJwtRequest(request));
            return ResponseEntity.ok(new TrainingStatusResponse(20001,"Fetch Records Successfully",trainingService.getTrainingStatusofUser(user_id)));
    }
    @GetMapping("/trainer/check")
    public ResponseEntity<?> getTrainerconfirmation(HttpServletRequest request){
            Long user_id=jwtTokenProvider.getUserIdFromToken(jwtAuthFilter.getJwtRequest(request));
            return trainingService.isTrainerCheck(user_id);
    }
    @PostMapping("/skillhub/nominate")
    public ResponseEntity<?> userRequestsforSkillhub(HttpServletRequest request,@RequestBody nominationsRequest nomineeReq){
        nomineeReq.setUser_id(jwtTokenProvider.getUserIdFromToken(jwtAuthFilter.getJwtRequest(request)));
        return userSkillsService.applySkillhubReqs(nomineeReq);
    }
    @GetMapping("/nomination/status")
    public ResponseEntity<?> getNominationStatus(HttpServletRequest request){
        Long user_id=jwtTokenProvider.getUserIdFromToken(jwtAuthFilter.getJwtRequest(request));
        return trainingService.getNominatedSkillsstatus(user_id);
    }
    @GetMapping("training/fetch")
    public TrainingsResponse getTraining(@RequestParam @PathVariable Long tr_id){
        return trainingService.getSpecificTraining(tr_id);
    }
    @PostMapping(value = "/cert/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> userCertUpload(HttpServletRequest request,@RequestPart("userCertRequest") UserCertRequest userCertRequest, @RequestParam(value = "certFile",required = false) MultipartFile file) throws IOException {
        System.out.println(userCertRequest.getCert_url());
        userCertRequest.setUser_id(jwtTokenProvider.getUserIdFromToken(jwtAuthFilter.getJwtRequest(request)));
        return userService.saveUserCertifications(userCertRequest,file);
    }
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getCertpic(@PathVariable String filename) throws MalformedURLException {
        Path filepath= Paths.get("C:\\Users\\w136637\\OneDrive - Worldline SA\\Bureau\\Backend-Main\\wgs_internalapp\\user-certificates").resolve(filename).normalize();
        Resource resource=new UrlResource(filepath.toUri());
        if(!resource.exists()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(resource);
    }
    @GetMapping("/getCertificates")
    public List<FetchUserCertificates> getCerts(HttpServletRequest request){
        return userService.getTrainingCertificates(jwtTokenProvider.getUserIdFromToken(jwtAuthFilter.getJwtRequest(request)));
    }
    @GetMapping("/getSpecificCertificate")
    public String getCertUrl(@RequestParam @PathVariable String cert_name){
        return userService.getTrainingCertificates(cert_name);
    }
    @PutMapping("/passwordReset")
    public ResponseEntity<?> passwordReset(HttpServletRequest request,@RequestBody PassowordResetRequest passowordResetRequest){
        Long user_id=jwtTokenProvider.getUserIdFromToken(jwtAuthFilter.getJwtRequest(request));
        return userService.passwordReset(user_id,passowordResetRequest);
    }
}
