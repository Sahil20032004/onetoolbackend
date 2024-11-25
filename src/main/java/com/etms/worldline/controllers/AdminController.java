package com.etms.worldline.controllers;


import com.etms.worldline.Service.EntityMasterService;
import com.etms.worldline.Service.TrainingService;
import com.etms.worldline.Service.UserService;
import com.etms.worldline.Service.UserSkillsService;
import com.etms.worldline.model.EntityMaster;
import com.etms.worldline.model.Nominations;
import com.etms.worldline.model.SkillSet;
import com.etms.worldline.model.User;
import com.etms.worldline.payload.request.NomineeReq;
import com.etms.worldline.payload.request.skilldetails;
import com.etms.worldline.payload.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin
@RequestMapping("/api/admin")
@Component
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserSkillsService userSkillsService;
    @Autowired
    private EntityMasterService entityMasterService;
    @Autowired
    private TrainingService trainingService;
    @GetMapping("/dashboard")
    public String sampleadmin(){
        return "Hello admin!";
    }
    @GetMapping("/allemployees")
    public List<User> getEmployees(){
        return userService.getEmployees();
    }
    @CrossOrigin
    @PutMapping("/verify")
    public ResponseEntity<?> updateUser(@RequestParam @PathVariable Long id){
        return userService.updateVerification(id,true);
    }
    @DeleteMapping("/cancel")
    public ResponseEntity<?> deleteUser(@RequestParam @PathVariable Long id){
            return userService.deleteUser(id);
    }
    @GetMapping("/getprojectid")
    public Projectdetails  getProjectId(@RequestParam @PathVariable String pro_name){
        return userService.getProjects(pro_name);
    }
    @GetMapping("/gblCount")
    public List<Integer> gblCount(@RequestParam @PathVariable String gbl){
        return userService.findthgblinfo(gbl);
    }
    @GetMapping("/getthegblmanagers")
    public List<User> getthegblmanager(@RequestParam @PathVariable String gbl){
        return userService.gettheempdetman(gbl);
    }
    @GetMapping("/getUsersunderManager")
    public List<User> getUsersunderMan(@RequestParam @PathVariable Long manager_id){
        return userService.getTheUsersUnderManager(manager_id);
    }
    @GetMapping("getempskill")
    public List<skilldetails> getempSkills(@RequestParam @PathVariable Long id){
        return userSkillsService.getempskill(id);
    }
    @GetMapping("/getemplackingskills")
    public List<lackingskilldetails> getemplackskills(@RequestParam @PathVariable Long id){
        return userSkillsService.getemplackingskills(id);
    }
    @GetMapping("getempid")
    public User getempidbyname(@RequestParam @PathVariable String name){
        return userService.getempid(name);
    }
    @GetMapping("getempidbyid")
    public User getempidbyid(@RequestParam @PathVariable Long id){
        return userService.getU(id);
    }
    @GetMapping("/gettheempundprojects")
    public List<Userdetails> gettheempsunderpro(@RequestParam @PathVariable String proName){
        return userService.getUsersbyProname(proName);
    }
    @PutMapping("/assign")
    public ResponseEntity<?> assignProject(@RequestParam @PathVariable Long user_id,@RequestParam @PathVariable Long pro_id,@RequestParam @PathVariable Long man_Id,@RequestParam @PathVariable boolean isTraining){
           return userService.assignProject(user_id, pro_id, man_Id, isTraining);
    }
    @GetMapping("/gettheempundmanprojects")
    public List<Userdetails> gettheempsunderprobyman(@RequestParam @PathVariable String proName,@RequestParam @PathVariable Long man_id){
        return userService.getUsersunderManagerbyProname(proName,man_id);
    }
    @GetMapping("/getProjectdetails")
    public List<Projectdetails> projectdetailsList(){
        return userService.getProjectdetailswithSkills();
    }
    @GetMapping("/getthegblprojects")
    public List<Projectdetails> getthegblproj(@RequestParam @PathVariable String gbl){
        return userService.getProjbyGbl(gbl);
    }
    @GetMapping("/getUsersProjects")
    public List<UserProjectDetails> projectDetails(@RequestParam @PathVariable Long id){
        return userService.getTheUsersProjects(id);
    }
    @GetMapping("/getAllEntitites")
    public List<EntityMaster> getAlltheentityMaster(){
        return entityMasterService.getAllentities();
    }
    @PutMapping("/nomineeAckAdm")
    public ResponseEntity<?> updateAckAdm(@RequestBody NomineeReq nomineeReq){
        System.out.println(nomineeReq.getUser_id());
        return trainingService.ackNominationsAdm(nomineeReq.getUser_id(),nomineeReq.getSkill_name(),nomineeReq.isAcknowledgement(),nomineeReq.getComments());
    }
    @GetMapping("/getPendingNominee")
    public List<Nominations> getNominees(){
        return trainingService.getPendingNomineesforAdmin();
    }
    @GetMapping("/getCertificates")
    public List<FetchUserCertificates> getCerts(@RequestParam @PathVariable Long user_id){
        return userService.getTrainingCertificates(user_id);
    }
    @GetMapping("/getSpecificCertificate")
    public String getCertUrl(@RequestParam @PathVariable String cert_name){
        return userService.getTrainingCertificates(cert_name);
    }
    @GetMapping("/getCertdetails")
    public List<CertificationsResponse> getCertdets(){
        return userService.getCertificates();
    }
}
