package com.etms.worldline.Service;


import com.etms.worldline.Repository.*;
import com.etms.worldline.model.*;
import com.etms.worldline.payload.request.NomineeReq;
import com.etms.worldline.payload.request.nominationsRequest;
import com.etms.worldline.payload.request.skilldetails;
import com.etms.worldline.payload.response.MessageResponse;
import com.etms.worldline.payload.response.Userdetails;
import com.etms.worldline.payload.response.lackingskilldetails;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserSkillsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private UserSkillsRepository userSkillsRepository;
    @Autowired
    private SkillHubRepository skillHubRepository;

    public List<UserSkills> showempskilllevel(Long id) {
        List<UserSkills> user = userSkillsRepository.findbyuserid(id);
        return user;
    }

    public List<lackingskilldetails> getemplackingskills(Long id) {
        List<lackingskilldetails> lackingskilldetailsList = userSkillsRepository.findtheallemplackingskillsbyid(id);
        System.out.println(lackingskilldetailsList);
        for (lackingskilldetails ls : lackingskilldetailsList) {
            int need_level = ls.getNeedskill_level();
            ls.setUrls(skillRepository.findBySkillName(ls.getSkillName()).get().getSkillLevels().stream().filter(matchedLevel -> matchedLevel.getLevel() == need_level).findFirst().get().getUrls());
        }
        return lackingskilldetailsList;
    }

    @Transactional
    public void updateUserskills(Long user_id, List<skilldetails> skilldetailsList) {
        for (skilldetails sd : skilldetailsList) {
            System.out.println(sd);
            User user = userRepository.findById(user_id).get();
            UserSkills userSkills = new UserSkills(user, skillRepository.findBySkillName(sd.getSkillName()).get(), sd.getSkill_level());
            userSkillsRepository.save(userSkills);
//            int updated = userSkillsRepository.updateSkillLevel(user_id, skillRepository.findBySkillName(sd.getSkillName()).get().getId(), sd.getSkill_level());
//            if (updated == 0) {
//              User user=userRepository.findById(user_id).get();
//              SkillSet skillSet=skillRepository.findBySkillName(sd.getSkillName()).get();
//              UserSkills userSkills=new UserSkills(user,skillSet, sd.getSkill_level());
//              userSkillsRepository.save(userSkills);
//            }
//            System.out.println(updated);
        }

    }

    public List<skilldetails> getempskill(Long user_id) {
        List<skilldetails> skills = userSkillsRepository.findtheempskillsbyid(user_id);
        return skills;
    }

    @Transactional
    public ResponseEntity<?> updatetrskillandlevel(Long id, List<skilldetails> skilldetailsList) {
        try {
            User user = userRepository.findById(id).get();
            for (skilldetails sd : skilldetailsList) {
                int updated = userSkillsRepository.updateTrainingandSetskilllevel(id, skillRepository.findBySkillName(sd.getSkillName()).get().getId(), sd.getSkill_level());
                if (updated == 0) {

                    SkillSet skillSet = skillRepository.findBySkillName(sd.getSkillName()).get();
                    UserSkills userSkills = new UserSkills(user, skillSet, sd.getSkill_level(), true);
                    userSkillsRepository.save(userSkills);
                }
            }
            return ResponseEntity.ok(new MessageResponse("Employee training skills and its level is updated successfully"));
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new MessageResponse(50000,"Something is wrong, Try again Later!"));}
        }


    public void assignTraining(Long user_id) {
        User user = userRepository.findById(user_id).get();
        user.setTraining(true);
        userRepository.save(user);
    }

    public List<skilldetails> getallempskills(Long id) {
        return userSkillsRepository.findtheallempskillsbyid(id);
    }

    public List<Userdetails> getAllLackingUsers() {
        List<Userdetails> users = userRepository.getUserslackinginskills();
        for (Userdetails user : users) {
            user.setLackingskilldetails(userSkillsRepository.findtheallemplackingskillsbyid(user.getUser_id()));
        }
        return users;
    }

    public ResponseEntity<?> applySkillhubReqs(nominationsRequest nomineeReq) {
        try {
            User user = userRepository.findById(nomineeReq.getUser_id()).get();
            for (String skill : nomineeReq.getSkills()) {
                SkillSet skillSet = skillRepository.findBySkillName(skill).get();
                SkillHub requests = new SkillHub();
                requests.setUser(user);
                requests.setSkillSet(skillSet);
                skillHubRepository.save(requests);
            }
            return ResponseEntity.ok(new MessageResponse(20001,"Registration Successful"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new MessageResponse(50000,"Internal Server error"));
        }
    }

    public List<String> getApprovedSkillHubSkills(Long user_id){
        return skillHubRepository.getApprovedSkillHubSkills(user_id);
    }
    public Map<String,Integer> getTheAvgskillsofEmpforGbl(String gbl){
        List<Object[]> avgskills=userSkillsRepository.getUserSkilldatasforGBL(gbl);
        Map<Long,Integer> avgSkilllevels=new HashMap<>();
        for(Object[] avgskill:avgskills){
            Long user_id=((Number) avgskill[0]).longValue();
            int skill_level=((Number) avgskill[1]).intValue();
            avgSkilllevels.put(user_id,skill_level);
        }
        Map<String, Integer> categorizedCounts = new HashMap<>();
        categorizedCounts.put("L0",0);
        categorizedCounts.put("L1", 0); // For levels 0 and 1
        categorizedCounts.put("L2", 0); // For level 2
        categorizedCounts.put("L3", 0); // For level 3
        categorizedCounts.put("L4", 0); // For level 4
        categorizedCounts.put("TOTAL",0);
        // Iterate through the skill levels and categorize them
        for (Integer skillLevel : avgSkilllevels.values()) {
            if (skillLevel == 0) {
                categorizedCounts.put("L0", categorizedCounts.get("L0") + 1);
            }else if (skillLevel == 1) {
                categorizedCounts.put("TOTAL",categorizedCounts.get("TOTAL")+1);
                categorizedCounts.put("L1", categorizedCounts.get("L1") + 1);
            } else if (skillLevel == 2) {
                categorizedCounts.put("TOTAL",categorizedCounts.get("TOTAL")+1);
                categorizedCounts.put("L2", categorizedCounts.get("L2") + 1);
            } else if (skillLevel == 3) {
                categorizedCounts.put("TOTAL",categorizedCounts.get("TOTAL")+1);
                categorizedCounts.put("L3", categorizedCounts.get("L3") + 1);
            } else if (skillLevel == 4) {
                categorizedCounts.put("TOTAL",categorizedCounts.get("TOTAL")+1);
                categorizedCounts.put("L4", categorizedCounts.get("L4") + 1);
            }
        }
        return categorizedCounts;
    }
    public Map<String,Integer> getTheFuncskillsofEmpforGbl(String gbl,String batch){
        List<Object[]> avgfuncskills=userSkillsRepository.getFunctionalSkillforEmployees(gbl,batch);
        Map<Long,Integer> funcSkilllevels=new HashMap<>();
        for(Object[] avgskill:avgfuncskills){
            Long user_id=((Number) avgskill[0]).longValue();
            int skill_level=((Number) avgskill[1]).intValue();
            funcSkilllevels.put(user_id,skill_level);
        }
        Map<String, Integer> categorizedCounts = new HashMap<>();
        categorizedCounts.put("L1", 0); // For levels 0 and 1
        categorizedCounts.put("L2", 0); // For level 2
        categorizedCounts.put("L3", 0); // For level 3
        categorizedCounts.put("L4", 0); // For level 4
        categorizedCounts.put("TOTAL",0);
        // Iterate through the skill levels and categorize them
        for (Integer skillLevel : funcSkilllevels.values()) {
            if (skillLevel == 0 || skillLevel == 1) {
                categorizedCounts.put("TOTAL",categorizedCounts.get("TOTAL")+1);
                categorizedCounts.put("L1", categorizedCounts.get("L1") + 1);
            } else if (skillLevel == 2) {
                categorizedCounts.put("TOTAL",categorizedCounts.get("TOTAL")+1);
                categorizedCounts.put("L2", categorizedCounts.get("L2") + 1);
            } else if (skillLevel == 3) {
                categorizedCounts.put("TOTAL",categorizedCounts.get("TOTAL")+1);
                categorizedCounts.put("L3", categorizedCounts.get("L3") + 1);
            } else if (skillLevel == 4) {
                categorizedCounts.put("TOTAL",categorizedCounts.get("TOTAL")+1);
                categorizedCounts.put("L4", categorizedCounts.get("L4") + 1);
            }
        }
        return categorizedCounts;
    }
    public Map<String,Integer> getTheGETskillsofEmpforGbl(String gbl,String batch){
        List<Object[]> avgtechskills=userSkillsRepository.getGETSkilldatasforGBL(gbl,batch);
        Map<Long,Integer> techSkilllevels=new HashMap<>();
        for(Object[] avgskill:avgtechskills){
            Long user_id=((Number) avgskill[0]).longValue();
            int skill_level=((Number) avgskill[1]).intValue();
            techSkilllevels.put(user_id,skill_level);
        }
        Map<String, Integer> categorizedCounts = new HashMap<>();
        categorizedCounts.put("L1", 0); // For levels 0 and 1
        categorizedCounts.put("L2", 0); // For level 2
        categorizedCounts.put("L3", 0); // For level 3
        categorizedCounts.put("L4", 0); // For level 4
        categorizedCounts.put("TOTAL",0);
        // Iterate through the skill levels and categorize them
        for (Integer skillLevel : techSkilllevels.values()) {
            if (skillLevel == 0 || skillLevel == 1) {
                categorizedCounts.put("TOTAL",categorizedCounts.get("TOTAL")+1);
                categorizedCounts.put("L1", categorizedCounts.get("L1") + 1);
            } else if (skillLevel == 2) {
                categorizedCounts.put("TOTAL",categorizedCounts.get("TOTAL")+1);
                categorizedCounts.put("L2", categorizedCounts.get("L2") + 1);
            } else if (skillLevel == 3) {
                categorizedCounts.put("TOTAL",categorizedCounts.get("TOTAL")+1);
                categorizedCounts.put("L3", categorizedCounts.get("L3") + 1);
            } else if (skillLevel == 4) {
                categorizedCounts.put("TOTAL",categorizedCounts.get("TOTAL")+1);
                categorizedCounts.put("L4", categorizedCounts.get("L4") + 1);
            }
        }
        return categorizedCounts;
    }
    public List<Map<String, Object>> getThenamesofEmpforGblwithLlevel(String gbl,String skill_level,boolean total) {
        List<Object[]> avgskills = userSkillsRepository.getUserSkilldatasforGBL(gbl);
        Map<Long, Integer> avgSkilllevels = new HashMap<>();

        // Fetch and map user IDs to their average skill levels
        for (Object[] avgskill : avgskills) {
            Long userId = ((Number) avgskill[0]).longValue();
            int skillLevel = ((Number) avgskill[1]).intValue();
            avgSkilllevels.put(userId, skillLevel);
        }
        if(total==true){
            List<Map<String, Object>> categorizedUsers = new ArrayList<>();
            for (Map.Entry<Long, Integer> entry : avgSkilllevels.entrySet()) {
                Long userId = entry.getKey();
                String userName = userRepository.findById(userId).get().getUsername(); // Assuming a method to fetch the user name by ID
                String usergbl=userRepository.getById(userId).getGbl();
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", userId);
                userMap.put("name", userName);
                userMap.put("gbl",usergbl);
                // Categorize based on skill level
                categorizedUsers.add(userMap);
            }

            return categorizedUsers;
        }
        // Initialize the structure for categorized counts
        Map<String, List<Map<String, Object>>> categorizedUsers = new HashMap<>();
        categorizedUsers.put("L0", new ArrayList<>());
        categorizedUsers.put("L1", new ArrayList<>()); // For levels 0 and 1
        categorizedUsers.put("L2", new ArrayList<>()); // For level 2
        categorizedUsers.put("L3", new ArrayList<>()); // For level 3
        categorizedUsers.put("L4", new ArrayList<>()); // For level 4

        // Iterate through the skill levels, fetch user names, and categorize them
        for (Map.Entry<Long, Integer> entry : avgSkilllevels.entrySet()) {
            Long userId = entry.getKey();
            int skillLevel = entry.getValue();
            String userName = userRepository.findById(userId).get().getUsername(); // Assuming a method to fetch the user name by ID
            String usergbl=userRepository.getById(userId).getGbl();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", userId);
            userMap.put("name", userName);
            userMap.put("gbl",usergbl);
            // Categorize based on skill level
            if (skillLevel == 0) {
                categorizedUsers.get("L0").add(userMap);
            } else if (skillLevel == 1) {
                categorizedUsers.get("L1").add(userMap);
            } else if (skillLevel == 2) {
                categorizedUsers.get("L2").add(userMap);
            } else if (skillLevel == 3) {
                categorizedUsers.get("L3").add(userMap);
            } else if (skillLevel == 4) {
                categorizedUsers.get("L4").add(userMap);
            }
        }

        return categorizedUsers.get(skill_level);
    }
    public List<Map<String, Object>> getThenamesofEmpforLO() {
        List<Object[]> avgskills = userSkillsRepository.getUserSkilldatas();
        Map<Long, Integer> avgSkilllevels = new HashMap<>();

        // Fetch and map user IDs to their average skill levels
        for (Object[] avgskill : avgskills) {
            Long userId = ((Number) avgskill[0]).longValue();
            int skillLevel = ((Number) avgskill[1]).intValue();
            avgSkilllevels.put(userId, skillLevel);
        }

        // Initialize the structure for categorized counts
        Map<String, List<Map<String, Object>>> categorizedUsers = new HashMap<>();
        categorizedUsers.put("L0", new ArrayList<>());// For level 4

        // Iterate through the skill levels, fetch user names, and categorize them
        for (Map.Entry<Long, Integer> entry : avgSkilllevels.entrySet()) {
            Long userId = entry.getKey();
            int skillLevel = entry.getValue();
            String userName = userRepository.findById(userId).get().getUsername(); // Assuming a method to fetch the user name by ID
            String usergbl=userRepository.getById(userId).getGbl();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", userId);
            userMap.put("name", userName);
            userMap.put("gbl",usergbl);
            // Categorize based on skill level
            if (skillLevel == 0) {
                categorizedUsers.get("L0").add(userMap);
            }
        }

        return categorizedUsers.get("L0");
    }

    public List<Map<String, Object>> getThenamesofEmpforGblwithLlevelGET(String gbl,String batch,String skill_level,boolean total) {
        List<Object[]> avgskills = userSkillsRepository.getGETSkilldatasforGBL(gbl,batch);
        Map<Long, Integer> avgSkilllevels = new HashMap<>();

        // Fetch and map user IDs to their average skill levels
        for (Object[] avgskill : avgskills) {
            Long userId = ((Number) avgskill[0]).longValue();
            int skillLevel = ((Number) avgskill[1]).intValue();
            avgSkilllevels.put(userId, skillLevel);
        }
        if(total==true){
            List<Map<String, Object>> categorizedUsers = new ArrayList<>();
            for (Map.Entry<Long, Integer> entry : avgSkilllevels.entrySet()) {
                Long userId = entry.getKey();
                String userName = userRepository.findById(userId).get().getUsername(); // Assuming a method to fetch the user name by ID
                String usergbl=userRepository.getById(userId).getGbl();
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", userId);
                userMap.put("name", userName);
                userMap.put("gbl",usergbl);
                // Categorize based on skill level
                categorizedUsers.add(userMap);
            }

            return categorizedUsers;
        }

        // Initialize the structure for categorized counts
        Map<String, List<Map<String, Object>>> categorizedUsers = new HashMap<>();
        categorizedUsers.put("L1", new ArrayList<>()); // For levels 0 and 1
        categorizedUsers.put("L2", new ArrayList<>()); // For level 2
        categorizedUsers.put("L3", new ArrayList<>()); // For level 3
        categorizedUsers.put("L4", new ArrayList<>()); // For level 4

        // Iterate through the skill levels, fetch user names, and categorize them
        for (Map.Entry<Long, Integer> entry : avgSkilllevels.entrySet()) {
            Long userId = entry.getKey();
            int skillLevel = entry.getValue();
            String userName = userRepository.findById(userId).get().getUsername(); // Assuming a method to fetch the user name by ID
            String usergbl=userRepository.getById(userId).getGbl();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", userId);
            userMap.put("name", userName);
            userMap.put("gbl",usergbl);
            // Categorize based on skill level
            if (skillLevel == 0 || skillLevel == 1) {
                categorizedUsers.get("L1").add(userMap);
            } else if (skillLevel == 2) {
                categorizedUsers.get("L2").add(userMap);
            } else if (skillLevel == 3) {
                categorizedUsers.get("L3").add(userMap);
            } else if (skillLevel == 4) {
                categorizedUsers.get("L4").add(userMap);
            }
        }

        return categorizedUsers.get(skill_level);
    }
    public List<Map<String, Object>> getThenamesofEmpforGblwithLlevelGETFunc(String gbl,String batch,String skill_level,boolean total) {
        List<Object[]> avgskills = userSkillsRepository.getFunctionalSkillforEmployees(gbl,batch);
        Map<Long, Integer> avgSkilllevels = new HashMap<>();

        // Fetch and map user IDs to their average skill levels
        for (Object[] avgskill : avgskills) {
            Long userId = ((Number) avgskill[0]).longValue();
            int skillLevel = ((Number) avgskill[1]).intValue();
            avgSkilllevels.put(userId, skillLevel);
        }
        if(total==true){
            List<Map<String, Object>> categorizedUsers = new ArrayList<>();
            for (Map.Entry<Long, Integer> entry : avgSkilllevels.entrySet()) {
                Long userId = entry.getKey();
                String userName = userRepository.findById(userId).get().getUsername(); // Assuming a method to fetch the user name by ID
                String usergbl=userRepository.getById(userId).getGbl();
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", userId);
                userMap.put("name", userName);
                userMap.put("gbl",usergbl);
                // Categorize based on skill level
                categorizedUsers.add(userMap);
            }

            return categorizedUsers;
        }

        // Initialize the structure for categorized counts
        Map<String, List<Map<String, Object>>> categorizedUsers = new HashMap<>();
        categorizedUsers.put("L1", new ArrayList<>()); // For levels 0 and 1
        categorizedUsers.put("L2", new ArrayList<>()); // For level 2
        categorizedUsers.put("L3", new ArrayList<>()); // For level 3
        categorizedUsers.put("L4", new ArrayList<>()); // For level 4

        // Iterate through the skill levels, fetch user names, and categorize them
        for (Map.Entry<Long, Integer> entry : avgSkilllevels.entrySet()) {
            Long userId = entry.getKey();
            int skillLevel = entry.getValue();
            String userName = userRepository.findById(userId).get().getUsername(); // Assuming a method to fetch the user name by ID
            String usergbl=userRepository.getById(userId).getGbl();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", userId);
            userMap.put("name", userName);
            userMap.put("gbl",usergbl);
            // Categorize based on skill level
            if (skillLevel == 0 || skillLevel == 1) {
                categorizedUsers.get("L1").add(userMap);
            } else if (skillLevel == 2) {
                categorizedUsers.get("L2").add(userMap);
            } else if (skillLevel == 3) {
                categorizedUsers.get("L3").add(userMap);
            } else if (skillLevel == 4) {
                categorizedUsers.get("L4").add(userMap);
            }
        }

        return categorizedUsers.get(skill_level);
    }

}
