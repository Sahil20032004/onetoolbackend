package com.etms.worldline.Service;

import com.etms.worldline.Repository.*;
import com.etms.worldline.model.*;
import com.etms.worldline.payload.request.*;
import com.etms.worldline.payload.response.*;
import com.etms.worldline.security.CustomUserDetailsService;
import com.etms.worldline.security.JwtTokenProvider;
import com.etms.worldline.security.UserPrincipal;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private UserSkillsRepository userSkillsRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private ProjectSkillsRepository projectSkillsRepository;
    @Autowired
    private UserProjectRepository userProjectRepository;
    @Autowired
    private SkillLevelwithUrlsRepository skillLevelwithUrlsRepository;
    @Autowired
    private UserCertificationsRepository userCertificationsRepository;
    @Autowired
    private CertificateMasterRepository certificateMasterRepository;
    @Autowired
    private CerttotalFundsRepo certtotalFundsRepo;
    private Firestore db;
    private Executor executor;
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/onetool-1e7be/messages:send";
    public ResponseEntity<?> registerUser(SignupRequest signupRequest, MultipartFile file) {
        try {
            // Check if the username already exists
            if (existsByUsername(signupRequest.getUsername())) {
                return ResponseEntity.ok(new MessageResponse("User is already there!"));
            }
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Please upload the User Profile Picture!"));
            }
            String bucket_name="competencygcp";
            String projectFolder = "user-photos";
            String projectName = signupRequest.getDas_id();

            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("/home/gcpticketingsystem_gmail_com/caramel-vim-438009-d4-8cdaf5d39f55.json")))
                    .build()
                    .getService();
            // Save the profile picture
            String objName = projectFolder + "/" + projectName + "/" + file.getOriginalFilename();
            BlobId blobId=BlobId.of(bucket_name,objName);
            BlobInfo blobInfo=BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
            storage.create(blobInfo,file.getBytes());
            String gcpFileUrl=String.format("https://storage.googleapis.com/%s/%s", bucket_name, objName);
            if(!isValidPassword(signupRequest.getPassword())){
                return ResponseEntity.badRequest().body(new MessageResponse(40000,"Password doesn't meet the requirements"));
            }
            // Create a new user with the details from the signup request
            User reguser = new User(signupRequest.getUsername(),
                    signupRequest.getEmail(),
                    encoder.encode(signupRequest.getPassword()),
                    signupRequest.getLast_name(),
                    signupRequest.getLocation(),
                    signupRequest.getDob(),
                    signupRequest.getEntity(),
                    signupRequest.getDas_id(),
                    signupRequest.getGender(),
                    signupRequest.getGbl());

            Set<String> strRoles = signupRequest.getRole();
            Set<Role> roles = new HashSet<>();

            // Assign default role if no role is specified
            if (strRoles == null) {
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            }
            reguser.setRoleName("USER");
            reguser.setRoles(roles);
            reguser.setPropicPath(gcpFileUrl);
            Set<Long> skillids = signupRequest.getEmpskillids();
            if (skillids.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Select the required skills to continue"));
            }
            // Save the user in the database
            User savedUser = userRepository.save(reguser);

            // Associate skills with the user
            Set<SkillSet> employee_skills = skillRepository.findAllById(skillids).stream().collect(Collectors.toSet());
            for (SkillSet skill : employee_skills) {
                UserSkills userSkills = new UserSkills(savedUser, skill);
                userSkillsRepository.save(userSkills);
            }

            return ResponseEntity.ok(new MessageResponse("Employee Registration Request is Sent"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error occurred while saving the profile picture"));
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error: Role is not found"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("An unexpected error occurred during registration"));
        }
    }

    public ResponseEntity<?> registerManager(SignupRequest signupRequest) {
        try {
            if (existsByUsername(signupRequest.getUsername())) {
                return ResponseEntity.ok(new MessageResponse("User is already there!"));
            }

            User reguser = new User(signupRequest.getUsername(),
                    signupRequest.getEmail(),
                    encoder.encode(signupRequest.getPassword()),
                    signupRequest.getLast_name(),
                    signupRequest.getLocation(),
                    signupRequest.getDob(),
                    signupRequest.getEntity(),
                    signupRequest.getDas_id(),
                    signupRequest.getGender(),
                    signupRequest.getGbl());
            Set<String> strRoles = signupRequest.getRole();
            Set<Role> roles = new HashSet<>();
            if (strRoles == null) {
                Role userRole = roleRepository.findByName(ERole.ROLE_MANAGER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            }

            reguser.setRoleName("MANAGER");
            reguser.setRoles(roles);
            save(reguser);


            return ResponseEntity.ok(new MessageResponse("Manager registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Something is wrong, Try again Later!"));
        }
    }

    public ResponseEntity<?> registerAdmin(SignupRequest signupRequest) {
        try {
            if (existsByUsername(signupRequest.getUsername())) {
                return ResponseEntity.ok(new MessageResponse("User is already there!"));
            }
            User reguser = new User(signupRequest.getUsername(),
                    signupRequest.getEmail(),
                    encoder.encode(signupRequest.getPassword()),
                    signupRequest.getLast_name(),
                    signupRequest.getLocation(),
                    signupRequest.getDob(),
                    signupRequest.getEntity(),
                    signupRequest.getDas_id(),
                    signupRequest.getGender(),
                    signupRequest.getGbl());
            Set<String> strRoles = signupRequest.getRole();
            Set<Role> roles = new HashSet<>();
            if (strRoles == null) {
                Role userRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            }
            reguser.setRoleName("ADMIN");
            reguser.setRoles(roles);
            save(reguser);
            return ResponseEntity.ok(new MessageResponse("Admin registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Something is wrong, Try again Later!"));
        }
    }
    public ResponseEntity<?> registerMaster(SignupRequest signupRequest){
        try {
            User reguser = new User(signupRequest.getUsername(),
                    signupRequest.getEmail(),
                    encoder.encode(signupRequest.getPassword()),
                    signupRequest.getLast_name(),
                    signupRequest.getLocation(),
                    signupRequest.getDob(),
                    signupRequest.getEntity(),
                    signupRequest.getDas_id(),
                    signupRequest.getGender(),
                    signupRequest.getGbl());
            Set<String> strRoles = signupRequest.getRole();
            Set<Role> roles = new HashSet<>();
            if (strRoles == null) {
                Role userRole = roleRepository.findByName(ERole.ROLE_MASTER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            }
            reguser.setRoleName("MASTERADMIN");
            reguser.setVerify(true);
            reguser.setRoles(roles);
            save(reguser);
            return ResponseEntity.ok(new MessageResponse("Master Admin registered successfully"));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(new MessageResponse("Something is wrong, Try again Later!"));}
    }
    public User save(User user){
        return userRepository.save(user);
    }
    public User getU(Long id){
        User user=userRepository.findById(id).get();
        return user;
    }
    public ResponseEntity<?> login(LoginRequest user){
        try {
            if (existsByEmail(user.getEmail())) {
                System.out.println(user.getEmail());
                if (checkVerifyEmail(user.getEmail())) {
                    // Fetch the user details from the database
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
                    // Check if the provided password matches the encoded password
                    if (encoder.matches(user.getPassword(), userDetails.getPassword())) {

                        User logUser=userRepository.findByEmail(user.getEmail()).get();
                        logUser.setLastLogin(LocalDateTime.now());
                         if(logUser.isFt_login()){
                             Authentication authentication = authenticationManager.authenticate(
                                     new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
                             SecurityContextHolder.getContext().setAuthentication(authentication);
                             String jwt = jwtTokenProvider.generateToken(authentication);
                             return ResponseEntity.ok(new LoginSuccessfullResponse(40002,"Password needs to change",jwt));
                         }
                        // Authenticate the user
                        Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        String jwt = jwtTokenProvider.generateToken(authentication);
                        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                        if(authentication.isAuthenticated()){
                            if(logUser.getUser_fcm_token()==null || !logUser.getUser_fcm_token().equals(user.getUser_fcm_token())){
                                logUser.setUser_fcm_token(user.getUser_fcm_token());
                            }
                        }
                        userRepository.save(logUser);
                        List<String> roles = userPrincipal.getAuthorities().stream()
                                .map(item -> item.getAuthority())
                                .collect(Collectors.toList());

//                    User employee = userRepository.findByEmail(user.getEmail()).get();
                        String managerToken;
                        ManagerdetailsDTO manager = null;
                        if(logUser.getRoleName().equals("USER")) {
                             managerToken = userRepository.getById(logUser.getManager_id()).getUser_fcm_token();
                            manager=new ManagerdetailsDTO(logUser.getUsername(),logUser.getEmail(),logUser.getDas_id(),logUser.getEntity(),logUser.getGbl(),logUser.getLocation(),logUser.getManager_name(),logUser.getManager_id(),managerToken);
                        }
                        else{
                            manager=new ManagerdetailsDTO(logUser.getUsername(),logUser.getEmail(),logUser.getDas_id(),logUser.getEntity(),logUser.getGbl(),logUser.getLocation(),logUser.getManager_name(),logUser.getManager_id(),null);
                        }


                        return ResponseEntity.ok(new LoginSuccessfullResponse(20000, "Login Successful", jwt, userPrincipal.getUserId(),roles, manager));
                    } else {
                        return ResponseEntity.status(401).body(new ErrorResponse(40100, "Invalid Credentials"));
                    }

                } else {
                    return ResponseEntity.ok().body(new ErrorResponse(40100, "User is not verified"));
                }
            }
            else {
                return ResponseEntity.ok().body(new ErrorResponse(40400, "User is not found"));
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ErrorResponse(50000,"An internal server error occurred. Please try again later"));
        }
    }
    public boolean existsByUsername(String username){
        return userRepository.findByUsername(username).isPresent();
    }
    public boolean existsByEmail(String email){
        System.out.println(email);
        return userRepository.findByEmail(email).isPresent();
    }
    public boolean checkVerify(String username){
        User user=userRepository.findByUsername(username).get();
        return user.isVerify();
    }
    public boolean checkVerifyEmail(String email){
        return true;
    }
    public ResponseEntity<?> updateVerification(Long id,boolean verify){
        try {
            User user = userRepository.findById(id).get();
            user.setVerify(verify);
            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse(20001,"Approval Successful"));
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body(new MessageResponse(50000,"Internal Server error"));
        }
    }
    public ResponseEntity<?> deleteUser(Long id){
        try {
            User user = userRepository.findById(id).get();
            user.setVerify(false);
            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse(20001,"Rejection Successful"));
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body(new MessageResponse(50000,"Internal Server error"));
        }
    }
    public List<User> getEmployees(){
        return userRepository.findAll();
    }
    public List<Userdetails> getUserdetailswithSkills(){
        List<Userdetails> userdetails=userRepository.getUsers();
        for(Userdetails userdetailswithskills:userdetails){
            userdetailswithskills.setSkilldetails(userSkillsRepository.findtheempskillsbyid(userdetailswithskills.getUser_id()));
        }
        return userdetails;
    }
    public List<Projectdetails> getProjectdetailswithSkills(){
        List<Projectdetails> prodetails=projectRepository.getProjects();
        for(Projectdetails prodetailswithskills:prodetails){
            ProjectAssign projectAssign=projectRepository.findById(prodetailswithskills.getPro_id()).get();
            prodetailswithskills.setDocUrls(projectAssign.getDocumentUrls());
            prodetailswithskills.setProjectskilldetails(projectSkillsRepository.findtheproskillsbyid(prodetailswithskills.getPro_id()));
        }
        return prodetails;
    }
    public List<Projectdetails> getProjectdetailswithSkillsunderManager(Long man_id){
        List<Projectdetails> prodetails=projectRepository.getProjectsByManager(man_id);
        for(Projectdetails prodetailswithskills:prodetails){
            ProjectAssign projectAssign=projectRepository.findById(prodetailswithskills.getPro_id()).get();
            prodetailswithskills.setDocUrls(projectAssign.getDocumentUrls());
            prodetailswithskills.setProjectskilldetails(projectSkillsRepository.findtheproskillsbyid(prodetailswithskills.getPro_id()));
        }
        return prodetails;
    }
    public ResponseEntity<?> assignProject(Long user_id,Long pro_id,Long man_id,boolean isTraining){
        try {
            User user = userRepository.findById(user_id).get();
            ProjectAssign projectAssign = projectRepository.findById(pro_id).get();
            if(user.getRoleName().equals("USER")) {
                String manager = userRepository.findById(man_id).get().getUsername();
                user.setAssign_project(projectAssign.getProName());
                user.setAssign_project("Yes");
                user.setManager_id(man_id);
                user.setManager_name(manager);
                user.setTraining(isTraining);
            }
            else if(user.getRoleName().equals("MANAGER")){
                user.setAssign_project(projectAssign.getProName());
                user.setAssign_project("Yes");
                user.setTraining(isTraining);
            }
            UserProject userProject = new UserProject(user, projectAssign, man_id, user.getRoleName());
            userRepository.save(user);
            userProjectRepository.save(userProject);
            return ResponseEntity.ok(new MessageResponse(20001,"Project,skills assigned successfully"));
        }
    catch (Exception e){
        return ResponseEntity.internalServerError().body(new MessageResponse(50000,"Something is wrong, try again later"));
    }
    }
    public List<ProjectAssign> getProjects(){
        return projectRepository.findAll();
    }
    public User assignSkills(Long user_id, String emp_group){
        User user=userRepository.findById(user_id).orElseThrow(()->new EntityNotFoundException("Employee not Found"));
        user.setGbl(emp_group);
        return userRepository.save(user);
    }
    public Projectdetails getProjects(String proName){
        Projectdetails pro=projectRepository.getProjectskById(proName);
        ProjectAssign projectAssign=projectRepository.findById(pro.getPro_id()).get();
        pro.setProjectskilldetails(projectSkillsRepository.findtheproskillsbyid(projectAssign.getPro_id()));
        return pro;


    }
    public SkillSet getSkills(String skillName){
        Optional<SkillSet> skill=skillRepository.findBySkillName(skillName);
        return skill.get();
    }
    public List<SkillSet> getSkills(){
        return skillRepository.findAll();
    }
    public ResponseEntity<?> fetchSkills(){
        try {
            List<SkillSet> skillSets = getSkills();
            if (skillSets.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(40400, "No records found"));
            }
            List<String> skills = skillSets.stream().map(item -> item.getSkillName()).collect(Collectors.toList());
            return ResponseEntity.ok(new SkillSetResponse(20001, "Fetched records Successfully", skills));
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body(new ErrorResponse(50000,"An internal server error occurred. Please try again later."));
        }
    }

    public ResponseEntity<?> saveSkills(SkillSet skill){
        if(skill==null){
            return ResponseEntity.badRequest().body(new MessageResponse(40000,"There is no skill is selected"));
        }
        try {
            skillRepository.save(skill);
            return ResponseEntity.ok(new MessageResponse(("Skill added successfully")));
        }
        catch (Exception e){
            return  ResponseEntity.internalServerError().body(new MessageResponse("Something is wrong, try again later"));
        }
    }
    public ResponseEntity<?> updateSkills(Long id, SkillSet skill){
        if(skill==null){
            return ResponseEntity.badRequest().body(new MessageResponse(40000,"There is no skill is selected"));
        }
        try {
            SkillSet newSkill = skillRepository.findById(id).get();
            newSkill.setSkillName(skill.getSkillName());
            newSkill.setSkillLevels(skill.getSkillLevels());
            skillRepository.save(newSkill);
            return ResponseEntity.ok(new MessageResponse(20001,"Skill updated successfully"));

        }
    catch (Exception e){
        return  ResponseEntity.internalServerError().body(new MessageResponse(50000,"Internal Server error"));
    }
    }
    public ResponseEntity<?> deleteSkill(Long id){
        try {
            skillRepository.deleteById(id);
            return ResponseEntity.ok(new MessageResponse(20001,"Skill deleted successfully"));
        }
          catch (Exception e){
            return  ResponseEntity.internalServerError().body(new MessageResponse(50000,"Internal Server error"));
        }
    }
    public User getempid(String username){
        User user=userRepository.findByUsername(username).get();
        return user;
    }

    public ProjectAssign saveProject(ProjectAssign pro){
        return projectRepository.save(pro);
    }
    public ResponseEntity<?> deleteProject(Long id){
        try {
            projectSkillsRepository.deleteByProjectId(id);
            projectRepository.deleteById(id);
            return ResponseEntity.ok(new MessageResponse(20001,"Project deleted successfully"));

        }
    catch (Exception e){
        return  ResponseEntity.internalServerError().body(new MessageResponse(50000,"Internal Server error"));
    }
    }
//    public void updateProject(Long id, projectsRequest pro){
//        ProjectAssign currpro=projectRepository.findById(id).get();
//        currpro.setProName(pro.getProName());
//        currpro.setProDesc(pro.getProDesc());
//        ProjectAssign savedProject=projectRepository.save(currpro);
//        for(projectskilldetails ps:pro.getProjectskilldetails()){
//            SkillSet skillSet=skillRepository.findBySkillName(ps.getSkill_name()).get();
//            ProjectSkills projectSkills=new ProjectSkills(savedProject,skillSet,ps.getSkillLevel());
//            projectSkillsRepository.save(projectSkills);
//        }
//        return projectRepository.save(currpro);
//    }
@Transactional
public ResponseEntity<?> updateProject(Long pro_id, Projectdetails project){
    if(project==null){
        return ResponseEntity.badRequest().body(new MessageResponse("There is no project details is given"));
    }
    try {
        List<ProjectSkills> skilldetailsList=projectSkillsRepository.findbyproid(pro_id);
        Set<String> updatedSkills=project.getProjectskilldetails().stream().map(projectskilldetails::getSkill_name).collect(Collectors.toSet());
        for(ProjectSkills currentUserskill:skilldetailsList){
            if(!updatedSkills.contains(currentUserskill.getSkillSet().getSkillName())){
                projectSkillsRepository.delete(currentUserskill);
            }
        }
        ProjectAssign pro=projectRepository.getById(pro_id);
        pro.setProName(project.getPro_name());
        pro.setProDesc(project.getProDesc());
        pro.setGbl(project.getProGbl());
        ProjectAssign savepro=projectRepository.save(pro);
        for(projectskilldetails sd:project.getProjectskilldetails()) {
            System.out.println(sd);

//        ProjectSkills proSkills=new UserSkills(pro,skillRepository.findBySkillName(sd.getSkill_name()).get(),sd.getSkillLevel());
//        userSkillsRepository.save(userSkills);
            int updated = projectSkillsRepository.updateSkillLevel(pro_id, skillRepository.findBySkillName(sd.getSkill_name()).get().getId(), sd.getSkillLevel());
            if (updated == 0) {
                SkillSet skillSet=skillRepository.findBySkillName(sd.getSkill_name()).get();
                ProjectSkills proSkills=new ProjectSkills(savepro,skillSet, sd.getSkillLevel());
                projectSkillsRepository.save(proSkills);
            }
            System.out.println(updated);
        }
        return ResponseEntity.ok(new MessageResponse(20001,"Project updated successfully"));

    }
    catch (Exception e){
        return  ResponseEntity.internalServerError().body(new MessageResponse(50000,"Internal Server error"));

    }


}
    @Transactional
    public void updateUserskills(Long user_id, List<skilldetails> skilldetailsList){
        User user=userRepository.getById(user_id);
        List<UserSkills> currentSkills=userSkillsRepository.findbyuserid(user_id);
         Set<String> updatedSkills=skilldetailsList.stream().map(skilldetails::getSkillName).collect(Collectors.toSet());
         for(UserSkills currentUserskill:currentSkills){
             if(!updatedSkills.contains(currentUserskill.getSkillSet().getSkillName())){
                 userSkillsRepository.delete(currentUserskill);
             }
         }

        for(skilldetails sd:skilldetailsList) {
            System.out.println(sd);

//        ProjectSkills proSkills=new UserSkills(pro,skillRepository.findBySkillName(sd.getSkill_name()).get(),sd.getSkillLevel());
//        userSkillsRepository.save(userSkills);
            int updated = userSkillsRepository.updateSkillLevel(user_id, skillRepository.findBySkillName(sd.getSkillName()).get().getId(), sd.getSkill_level());
            if (updated == 0) {
                SkillSet skillSet=skillRepository.findBySkillName(sd.getSkillName()).get();
                UserSkills userSkills=new UserSkills(user,skillSet, sd.getSkill_level());
                userSkillsRepository.save(userSkills);
            }
            System.out.println(updated);
        }

    }

    public ProjectAssign getProjectById(Long id){
        ProjectAssign pro=projectRepository.findById(id).get();
        return pro;
    }
//    public User updateemployeeSkill(Long user_id,Set<Long> skillids){
//        User user=userRepository.findById(user_id).get();
//        Set<SkillSet> skills=skillRepository.findAllById(skillids).stream().collect(Collectors.toSet());
//        user.setEmployeeskills(skills);
//        return userRepository.save(user);
//    }
    public ResponseEntity<?> quizCompletion(Long user_id){
        try {
            User user = userRepository.findById(user_id).get();
            user.setQuiz_status("Completed");
            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse(20001,"Quiz Completed Successfully"));
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body(new MessageResponse(50000,"Internal Server error"));
        }
        }
    public List<User> getTheUsersUnderManager(Long manager_id){
        return userRepository.findtheemployeesunderManager(manager_id);
    }
    public List<UserProjectDetails> getTheUsersProjects(Long user_id){
        return userProjectRepository.userProjectDetails(user_id);
    }
    @Transactional
    public int updateInActiveUser(Long user_id,Long pro_id,boolean isActive){
        return userProjectRepository.updateUserprojectDet(user_id,pro_id,false);
    }
    public List<Integer> findthgblinfo(String gbl){
        List<Integer> countList=new ArrayList<>();
        int empcount=userRepository.findthegblcountofemployees(gbl);
        int empnotinpro=userRepository.findtheempnotinpro(gbl);
        int proingbl=projectRepository.findthegblcountofprojects(gbl);
        List<Object[]> lackInfo=userSkillsRepository.findTop4SkillsLackingByUsers();
        countList.add(empcount);
        countList.add(empnotinpro);
        countList.add(proingbl);
        return countList;
    }
    public Projectdetails findtheProinfbyId(Long pro_id){
        Projectdetails projectdetails=new Projectdetails();
        ProjectAssign projectAssign=projectRepository.getById(pro_id);
        projectdetails.setProGbl(projectAssign.getGbl());
        projectdetails.setProDesc(projectAssign.getProDesc());
        projectdetails.setPro_id(projectAssign.getPro_id());
        projectdetails.setPro_name(projectAssign.getProName());
        projectdetails.setDocUrls(projectAssign.getDocumentUrls());
        projectdetails.setProjectskilldetails(projectSkillsRepository.findtheproskillsbyid(pro_id));
        return projectdetails;
    }
    public List<User> gettheempdetman(String gbl){
        return userRepository.findthegblemployeesman(gbl);
    }
    public List<Projectdetails> getProjbyGbl(String gbl){
        return projectRepository.getProjectsBygl(gbl);
    }
//    public List<Userdetails> getUsersbyProname(String proName){
//        return userProjectRepository.usersInProject(proName);
//    }
    public List<Userdetails> getUsersbyProname(String proName){
        return userProjectRepository.managerInProject(proName);
    }
    public List<Userdetails> getUsersunderManagerbyProname(String proName, Long man_id){
        return userProjectRepository.usersInProject(proName,man_id);
    }
    public DashboardDetails getTheDashDetails(){
        DashboardDetails dashboardDetails=new DashboardDetails();
        dashboardDetails.setTotalEmployees(userRepository.findthecountofemployees());
        dashboardDetails.setTotalManagers(userRepository.findthecountemployeesman());
        dashboardDetails.setTotalProjects(projectRepository.findthecountofprojects());
        dashboardDetails.setTotalSkills(skillRepository.findthecountofskills());
        dashboardDetails.setTopSkillsUtilisedByUser(userSkillsRepository.findTop4SkillsultilizedByUsers());
        dashboardDetails.setTop4skillslackedbyUser(userSkillsRepository.findTop4SkillsLackingByUsers());
        dashboardDetails.setSkillsfromMosttoLeast(userSkillsRepository.findAlltheskillmosttoleastutilised());
        dashboardDetails.setRecentlyAddedUsers(userRepository.getRecentJoinedUsers());
        dashboardDetails.setRecentlyAddedProjects(projectRepository.getProjectsByDate());
        dashboardDetails.setTotalmembersinEachProject(userProjectRepository.getProjectMembers());
        return dashboardDetails;
    }
    public DashboardDetails getTheDashDetailsbyGbl(String gbl){
        DashboardDetails dashboardDetails=new DashboardDetails();
        dashboardDetails.setTotalEmployees(userRepository.findthegblcountofemployees(gbl));
        dashboardDetails.setTotalManagers(userRepository.findthegblcountemployeesman(gbl));
        dashboardDetails.setTotalProjects(projectRepository.findthegblcountofprojects(gbl));
        dashboardDetails.setTotalSkills(skillRepository.findthecountofskills());
        dashboardDetails.setTopSkillsUtilisedByUser(userSkillsRepository.findTop4SkillsutilisedByUserswithgbl(gbl));
        dashboardDetails.setTop4skillslackedbyUser(userSkillsRepository.findTop4SkillsLackingByUserswithgbl(gbl));
        dashboardDetails.setSkillsfromMosttoLeast(userSkillsRepository.findAlltheskillmosttoleastutilisedbygbl(gbl));
        dashboardDetails.setRecentlyAddedUsers(userRepository.getRecentJoinedUsersbyGbl(gbl));
        dashboardDetails.setRecentlyAddedProjects(projectRepository.getProjectsByDatebygbl(gbl));
        dashboardDetails.setTotalmembersinEachProject(userProjectRepository.getProjectMembersbyGbl(gbl));
        return dashboardDetails;
    }
    public ResponseEntity<?> saveUserCertifications(UserCertRequest userCertRequest,MultipartFile file) throws IOException {
        try {
            User user = userRepository.findById(userCertRequest.getUser_id()).get();
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Please upload the Certificate"));
            }

            // Save the profile picture
            String bucket_name="competencygcp";
            String projectFolder = "user-certificates";
            String projectName = user.getDas_id();

            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("/home/gcpticketingsystem_gmail_com/caramel-vim-438009-d4-8cdaf5d39f55.json")))
                    .build()
                    .getService();
            // Save the profile picture
            CertificateMaster certificateMaster=certificateMasterRepository.getById(userCertRequest.getCert_id());
            CertificateFunds totalFunds=certtotalFundsRepo.getById(1L);
            double usedFunds=userCertificationsRepository.getUsedFunds();
            if(usedFunds>=totalFunds.getTotalFunds()){
                return ResponseEntity.ok(new MessageResponse(40000,"Insufficient funds"));
            }
            String objName = projectFolder + "/" + projectName + "/" + file.getOriginalFilename();
            BlobId blobId=BlobId.of(bucket_name,objName);
            BlobInfo blobInfo=BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
            storage.create(blobInfo,file.getBytes());
            String gcpFileUrl=String.format("https://storage.googleapis.com/%s/%s", bucket_name, objName);
            UserCertifications userCertifications = new UserCertifications(user,certificateMaster,userCertRequest.getCert_url(),gcpFileUrl,"completed");
            userCertificationsRepository.save(userCertifications);
            return ResponseEntity.ok(new MessageResponse(20001, "Certificates uploaded Successfully"));
        } catch (Exception e) {
          e.printStackTrace();
          return ResponseEntity.internalServerError().body(new MessageResponse(50000,"Internal Server error"));
        }

    }
    public List<FetchUserCertificates> getTrainingCertificates(Long user_id){
       return userCertificationsRepository.getUserCertificatesdetails(user_id);
    }
    public String getTrainingCertificates(String cert_name){
        return userCertificationsRepository.findByCert_name(cert_name).get().getCert_file_path();
    }
    public ResponseEntity<?> passwordReset(Long user_id,@RequestBody PassowordResetRequest passowordResetRequest){
        User user=userRepository.findById(user_id).get();
        if(passowordResetRequest.getNew_password()!=null && passowordResetRequest.getConfirm_password()!=null){
            if(isValidPassword(passowordResetRequest.getNew_password())){
        if(passowordResetRequest.getNew_password().equals(passowordResetRequest.getConfirm_password())){
            System.out.println(user.getDas_id());
            user.setPassword(encoder.encode(passowordResetRequest.getNew_password()));
            user.setFt_login(false);
            userRepository.save(user);
            return ResponseEntity.ok().body(new MessageResponse(20001,"Password changed successfully"));
        }else{
            return ResponseEntity.ok().body(new MessageResponse(40002,"Password does not match"));
        }
        }else{
                return ResponseEntity.ok().body(new MessageResponse(40000,"Password is not entered correctly with given requirements"));
            }}
        else{
          return ResponseEntity.ok().body(new MessageResponse(40002,"Enter the password"));
        }
           }
           public List<CertificationsResponse> getCertificates(){
            return userCertificationsRepository.getUserCertificatesCount();
           }
    public List<CertificationsResponse> getCertificatesforManager(Long manager_id){
        return userCertificationsRepository.getReportUserCertificatesCount(manager_id);
    }
    private boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,15}$";
        return password.matches(passwordRegex);
    }
    private String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new FileInputStream("/home/gcpticketingsystem_gmail_com/service_account.json"))
                .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));
        googleCredentials.refresh();
        return googleCredentials.getAccessToken().getTokenValue();
    }
    public ResponseEntity<?> sendNotification(String token,String title,String body,String userId) throws IOException{
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setBody(body);


        DataRequest data = new DataRequest();
        data.setTitle(title);
        data.setBody(body);
        data.setAction("OPEN_APP");

        Message message = new Message();
        message.setToken(token);
        message.setNotification(notification);
        message.setData(data);
        FcmMessage fcMmessage=new FcmMessage();
        fcMmessage.setMessage(message);
        System.out.println(fcMmessage);
        Firestore firestore=FirestoreClient.getFirestore();
        System.out.println(firestore);
        DocumentReference userDoc = firestore.collection("OneTool").document(userId);


        // If the user document exists, add the notification to the "notifications" subcollection
        CollectionReference notificationsCollection = userDoc.collection("notifications");
        notificationsCollection.add(fcMmessage)
                .isDone();

        RestTemplate restTemplate=new RestTemplate();
        HttpHeaders headers=new HttpHeaders();
        headers.set("Authorization","Bearer "+getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        System.out.println(message);
        HttpEntity<FcmMessage> entity = new HttpEntity<>(fcMmessage, headers);
        System.out.println(entity);
        ResponseEntity<String> response=null;
        try {
            response = restTemplate.exchange(FCM_URL, HttpMethod.POST, entity, String.class);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        if(response.getStatusCode()==HttpStatus.OK){
            return ResponseEntity.ok(new MessageResponse(20001,"Done"));
        }
        else{
            return ResponseEntity.ok(new MessageResponse(40001,"Unauthorized"));
        }
    }

}
