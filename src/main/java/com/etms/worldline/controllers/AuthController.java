package com.etms.worldline.controllers;

import com.etms.worldline.Repository.*;
import com.etms.worldline.Service.*;
import com.etms.worldline.model.*;
import com.etms.worldline.payload.request.*;
import com.etms.worldline.payload.response.*;
import com.etms.worldline.security.CustomUserDetailsService;
import com.etms.worldline.security.JwtTokenProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin
@Component
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private ExcelHandlingService excelHandlingService;
    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserSkillsService userSkillsService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private QuizService quizService;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private UserSkillsRepository userSkillsRepository;
    @Autowired
    private QuizforSkillRepository quizforSkillRepository;
    @Autowired
    private ProjectSkillsRepository projectSkillsRepository;
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EntityMasterService entityMasterService;
    @Autowired
    private EntityMasterRepository entityMasterRepository;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private UserProjectRepository userProjectRepository;
    @Autowired
    private SOSService sosService;
    @Autowired
    private CertificateMasterService certificateMasterService;
@PostMapping(value="/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<?> registerUser(@RequestPart("signupRequest") SignupRequest signupRequest,
                                      @RequestParam(value = "profilepic",required = false) MultipartFile file) {
    return userService.registerUser(signupRequest, file);
}

@PostMapping("/login")
public ResponseEntity<?> authUser(@RequestBody LoginRequest user) throws MalformedJwtException {
   return userService.login(user);
}

@CrossOrigin(origins = "http://localhost:4200")
@PostMapping("/sendNotification")
public ResponseEntity<?> getToken(@RequestBody FcmMessage notifications) throws IOException{
   return userService.sendNotification(notifications.getMessage().getToken(),notifications.getMessage().getNotification().getTitle(),notifications.getMessage().getData().getBody(),notifications.getMessage().getData().getUser_id());
}

    @PostMapping("/manager-register")
    public ResponseEntity<?> registerManager(@RequestBody SignupRequest signupRequest) {
        return userService.registerManager(signupRequest);
}

    @PostMapping("/admin-register")
    public ResponseEntity<?> registerAdmin(@RequestBody SignupRequest signupRequest) {
        return userService.registerAdmin(signupRequest);
    }

    @PostMapping("/master-register")
    public ResponseEntity<?> registerMasterAdmin(@RequestBody SignupRequest signupRequest) {
       return userService.registerMaster(signupRequest);
    }

    @GetMapping("/getskills")
    public List<SkillSet> getSkills() {
        return userService.getSkills();
    }

    @GetMapping("/getskillsid")
    public SkillSet getSkillId(@RequestParam @PathVariable String skillName) {
        return userService.getSkills(skillName);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file) {
       return excelHandlingService.uploadExcel(file);

    }
    @GetMapping("/getAllEntitites")
    public List<EntityMaster> getAlltheentityMaster(){
        return entityMasterService.getAllentities();
    }

    @PostMapping("/entityMasterUpload")
    public ResponseEntity<?> handleFileUploadinEntity(@RequestParam("file") MultipartFile file) {
       return excelHandlingService.entityExcelUpload(file);
    }


    @PutMapping("/updatetrainskillandlevel")
    public ResponseEntity<?> updateskillandlevel(@RequestParam @PathVariable Long id,@RequestBody List<skilldetails> skilldetails){
           return userSkillsService.updatetrskillandlevel(id,skilldetails);
    }
@GetMapping("/getUserwithskills")
public List<Userdetails> userdetailsList(){
    return userService.getUserdetailswithSkills();
}

    @GetMapping("/getProjectdetails")
    public List<Projectdetails> projectdetailsList(){
        return userService.getProjectdetailswithSkills();
    }

    @GetMapping("/gblCount")
    public List<Integer> gblCount(@RequestParam @PathVariable String gbl){
        return userService.findthgblinfo(gbl);
    }

    @GetMapping("/getthegblmanagers")
    public List<User> getthegblmanager(@RequestParam @PathVariable String gbl){
        return userService.gettheempdetman(gbl);
    }
    @GetMapping("/getthegblprojects")
    public List<Projectdetails> getthegblproj(@RequestParam @PathVariable String gbl){
        return userService.getProjbyGbl(gbl);
    }

    @PostMapping("/saveQuestions")
    public ResponseEntity<?> saveQuestions(@RequestBody List<SOSQuestions> sosquestions){
    return sosService.saveSosQuestions(sosquestions);
    }
    @PostMapping("/saveAnnouncements")
    public ResponseEntity<?> saveAnnouncements(@RequestBody List<Announcements> sosannouncements){
        return sosService.saveAnnouncements(sosannouncements);
    }
    @GetMapping("/getQuestions")
    public ResponseEntity<?> getQuestions(@RequestParam @PathVariable String location){
    return sosService.getSocQuestions(location);
    }
    @PostMapping("/saveResponses")
    public ResponseEntity<?> saveResponses(@RequestBody SOSsaveResponses soSsaveResponses){
    return sosService.saveResponses(soSsaveResponses);
    }
    @GetMapping("/getAlllackingusers")
    public List<Userdetails> getthelackusers(){
        return userSkillsService.getAllLackingUsers();
    }
   @GetMapping("/profile/{filename}")
   public ResponseEntity<Resource> getProfilepic(@PathVariable String filename) throws MalformedURLException {
        Path filepath= Paths.get("C:\\Users\\w136637\\OneDrive - Worldline SA\\Bureau\\Backend-Main\\wgs_internalapp\\user-photos").resolve(filename).normalize();
        Resource resource=new UrlResource(filepath.toUri());
        if(!resource.exists()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
   }

    @GetMapping("/dashboardDetails")
    public DashboardDetails getSamp(){
        return userService.getTheDashDetails();
    }
    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getCertpic(@PathVariable String filename) throws MalformedURLException {
        Path filepath= Paths.get("C:\\Users\\w136637\\OneDrive - Worldline SA\\Bureau\\Backend-Main\\wgs_internalapp\\user-certificates").resolve(filename).normalize();
        Resource resource=new UrlResource(filepath.toUri());
        if(!resource.exists()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
    }
    @GetMapping("/project/{filename}")
    public ResponseEntity<Resource> getProject(@PathVariable String filename) throws MalformedURLException {
        Path filepath= Paths.get("C:\\Users\\w136637\\OneDrive - Worldline SA\\Bureau\\Backend-Main\\wgs_internalapp\\user-projects").resolve(filename).normalize();
        Resource resource=new UrlResource(filepath.toUri());
        if(!resource.exists()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(resource);
    }



}


