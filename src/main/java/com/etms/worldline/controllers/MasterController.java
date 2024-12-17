package com.etms.worldline.controllers;

import com.etms.worldline.Repository.EntityMasterRepository;
import com.etms.worldline.Repository.ProjectRepository;
import com.etms.worldline.Repository.ProjectSkillsRepository;
import com.etms.worldline.Repository.SkillRepository;
import com.etms.worldline.Service.CertificateMasterService;
import com.etms.worldline.Service.EntityMasterService;
import com.etms.worldline.Service.ExcelHandlingService;
import com.etms.worldline.Service.UserService;
import com.etms.worldline.model.*;
import com.etms.worldline.payload.request.projectsRequest;
import com.etms.worldline.payload.request.projectskilldetails;
import com.etms.worldline.payload.response.DashboardDetails;
import com.etms.worldline.payload.response.MessageResponse;
import com.etms.worldline.payload.response.Projectdetails;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
@RestController
@CrossOrigin
@RequestMapping("/api/master")
public class MasterController {
    @Autowired
    private UserService userService;
    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private ProjectSkillsRepository projectSkillsRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private EntityMasterService entityMasterService;
    @Autowired
    private EntityMasterRepository entityMasterRepository;
    @Autowired
    private ExcelHandlingService excelHandlingService;
    @Autowired
    private CertificateMasterService certificateMasterService;
    @GetMapping("/sample")
    public String getSample(){
        return "Hello";
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
    @GetMapping("/getskills")
    public List<SkillSet> getSkills(){ return userService.getSkills();
    }
    @GetMapping("/getskillsid")
    public SkillSet getSkillId(@RequestParam @PathVariable String skillName){
        return userService.getSkills(skillName);
    }
    @PostMapping("/saveskill")
    public ResponseEntity<?> saveSkill(@RequestBody SkillSet skill){
       return userService.saveSkills(skill);
    }
   @PutMapping("/updateSkill")
    public ResponseEntity<?> updateSkill(@RequestParam @PathVariable Long id,@RequestBody SkillSet skill){
       return userService.updateSkills(id,skill);
   }
   @DeleteMapping("/deleteSkill")
    public ResponseEntity<?> deleteSkill(@RequestParam @PathVariable Long id){
        return userService.deleteSkill(id);
    }

    @DeleteMapping("/deleteProject")
    public ResponseEntity<?> deleteProject(@RequestParam @PathVariable Long id){
            return userService.deleteProject(id);
    }
   @PutMapping("/updateProject")
    public ResponseEntity<?> updateProject(@RequestParam @PathVariable Long id,@RequestBody Projectdetails projectskilldetails){
            return userService.updateProject(id, projectskilldetails);
    }

    @GetMapping("/getprojects")
    public List<ProjectAssign> getProjects(){
        return userService.getProjects();
    }
    @GetMapping("/getprojectid")
    public Projectdetails  getProjectId(@RequestParam @PathVariable String pro_name){
        return userService.getProjects(pro_name);
    }
    @GetMapping("/getProdetbyid")
    public ProjectAssign getProdddetById(@RequestParam @PathVariable Long pro_id){
        return userService.getProjectById(pro_id);
    }
    @GetMapping("/getProjectdetails")
    public List<Projectdetails> projectdetailsList(){
        return userService.getProjectdetailswithSkills();
    }
    @GetMapping("/getprodettById")
    public Projectdetails getProdetById(@RequestParam @PathVariable Long pro_id){
        return userService.findtheProinfbyId(pro_id);
    }
    @PostMapping(value = "/saveProject",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> saveProject(@RequestPart("project") projectsRequest pro, @RequestPart("files")List<MultipartFile> files) throws IOException {
        List<String> docsurls=new ArrayList<>();
        System.out.println(pro);
        String bucket_name="competencygcp";
        String projectFolder = "projects";  // Base project folder
        String projectName = pro.getProName();

        Storage storage = StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("/home/gcpticketingsystem_gmail_com/caramel-vim-438009-d4-8cdaf5d39f55.json")))
                .build()
                .getService();
        for(MultipartFile multipartFile:files){
            String objName = projectFolder + "/" + projectName + "/" + multipartFile.getOriginalFilename();
            BlobId blobId=BlobId.of(bucket_name,objName);
            BlobInfo blobInfo=BlobInfo.newBuilder(blobId).setContentType(multipartFile.getContentType()).build();
            storage.create(blobInfo,multipartFile.getBytes());
            String gcpFileUrl=String.format("https://storage.googleapis.com/%s/%s", bucket_name, objName);
            docsurls.add(gcpFileUrl);
        }
        ProjectAssign saveePro=new ProjectAssign(pro.getProName(),pro.getGbl(),pro.getProDesc(),pro.getMan_id());
        saveePro.setDocumentUrls(docsurls);
        ProjectAssign project= userService.saveProject(saveePro);
        for(projectskilldetails ps:pro.getProjectskilldetails()){
            SkillSet skillSet=skillRepository.findBySkillName(ps.getSkill_name()).get();
            ProjectSkills projectSkills=new ProjectSkills(project,skillSet,ps.getSkillLevel());
            projectSkillsRepository.save(projectSkills);
        }

        return ResponseEntity.ok(new MessageResponse("Project added successfully"));
    }
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getProDocs(@PathVariable String filename) throws MalformedURLException {
        Path filepath= Paths.get("C:\\Users\\w136637\\OneDrive - Worldline SA\\Bureau\\worldline\\user-projects\\").resolve(filename).normalize();
        Resource resource=new UrlResource(filepath.toUri());
        if(!resource.exists()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(resource);
    }
    @GetMapping("/getAllEntitites")
    public List<EntityMaster> getAlltheentityMaster(){
        return entityMasterService.getAllentities();
    }
    @PostMapping("/createEntity")
    public EntityMaster createtheEntity(@RequestBody EntityMaster entityMaster){
        return entityMasterRepository.save(entityMaster);
    }
    @PutMapping("/updateEntity")
    public EntityMaster updateTheEntity(@RequestParam @PathVariable Long en_id,@RequestBody EntityMaster entityMaster){
        return entityMasterService.updateEntityMaster(en_id,entityMaster);
    }
    @DeleteMapping("/deleteEntity")
    public ResponseEntity<?> deleteTheEntity(@RequestParam @PathVariable Long en_id){
        entityMasterService.deleteEntityMaster(en_id);
        return ResponseEntity.ok(new MessageResponse("Entity is deleted successfully"));
    }
    @PostMapping("/skillMasterUpload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file) {
        return excelHandlingService.handleFileUploadSkillMaster(file);
    }
    @PostMapping("/entityMasterUpload")
    public ResponseEntity<?> handleFileUploadinEntity(@RequestParam("file") MultipartFile file) {
   return excelHandlingService.entityExcelUpload(file);
    }

    @PostMapping("/uploadprojectMaster")
    public ResponseEntity<?> handleFileUploadProject(@RequestParam("file") MultipartFile file) {
          return excelHandlingService.handleFileUploadProject(file);

    }
    @GetMapping("/dashboardDetails")
    public DashboardDetails getSamp(){
        return userService.getTheDashDetails();
    }
    @PostMapping("/saveCertificateMaster")
    public ResponseEntity<?> saveCertMaster(@RequestBody CertificateMaster certificateMaster){
        return certificateMasterService.saveCertificate(certificateMaster);
    }
    @GetMapping("/getCertificateMaster")
    public List<CertificateMaster> saveCertMaster(){
        return certificateMasterService.getCertificatesfromMaster();
    }
    @PutMapping("/updateCertificateMaster")
    public ResponseEntity<?> updateCertMaster(@RequestBody CertificateMaster certificateMaster){
        return certificateMasterService.updateCertificate(certificateMaster);
    }
    @DeleteMapping("/deleteCertificateMaster")
    public ResponseEntity<?> deleteCertMaster(@RequestParam @PathVariable Long cert_id){
        return certificateMasterService.deleteCertificate(cert_id);
    }
}
