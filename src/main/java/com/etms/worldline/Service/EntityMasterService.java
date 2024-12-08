package com.etms.worldline.Service;

import com.etms.worldline.Repository.EntityMasterRepository;
import com.etms.worldline.model.EntityMaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EntityMasterService {
     @Autowired
     private EntityMasterRepository entityMasterRepository;
     @Autowired
     private UserSkillsService userSkillsService;

     public List<EntityMaster> getAllentities(){
         return entityMasterRepository.findAll();
     }
     public EntityMaster getEntitymasterbyId(Long en_id){
         EntityMaster entityMaster=entityMasterRepository.getById(en_id);
         return entityMaster;
     }
     public EntityMaster updateEntityMaster(Long en_id,EntityMaster entityMasterinput){
         EntityMaster entityMaster=entityMasterRepository.getById(en_id);
         entityMaster.setGblNames(entityMasterinput.getGblNames());
         return entityMasterRepository.save(entityMaster);
     }
     public void deleteEntityMaster(Long en_id){
         EntityMaster entityMaster=entityMasterRepository.getById(en_id);
         entityMasterRepository.delete(entityMaster);
     }
     public List<Map<String, Map<String,Integer>>> getAvgskillsLevelsofAllgbls(){
         List<String> allGbls=new ArrayList<>();
         allGbls=getAllentities().stream().map(gbl->gbl.getGblNames()).collect(Collectors.toList());
         System.out.println(allGbls);
         Map<String,Map<String,Integer>> gblDataSkilllevels=new HashMap<>();
         List<Map<String, Map<String,Integer>>> getGblDatasSkilllevels=new ArrayList<>();
         for(String gbl:allGbls){
             Map<String,Integer> gblData=userSkillsService.getTheAvgskillsofEmpforGbl(gbl);
             gblDataSkilllevels.put(gbl,gblData);
         }
         getGblDatasSkilllevels.add(gblDataSkilllevels);
         return getGblDatasSkilllevels;
     }
    public List<Map<String, Map<String,Integer>>> getfunckillsLevelsofAllgbls(String batch){
        List<String> allGbls=new ArrayList<>();
        allGbls=getAllentities().stream().map(gbl->gbl.getGblNames()).collect(Collectors.toList());
        System.out.println(allGbls);
        Map<String,Map<String,Integer>> gblDataSkilllevels=new HashMap<>();
        List<Map<String, Map<String,Integer>>> getGblDatasSkilllevels=new ArrayList<>();
        for(String gbl:allGbls){
            Map<String,Integer> gblData=userSkillsService.getTheFuncskillsofEmpforGbl(gbl,batch);
            gblDataSkilllevels.put(gbl,gblData);
        }
        getGblDatasSkilllevels.add(gblDataSkilllevels);
        return getGblDatasSkilllevels;
    }
    public List<Map<String, Map<String,Integer>>> getGETskillsLevelsofAllgbls(String batch){
        List<String> allGbls=new ArrayList<>();
        allGbls=getAllentities().stream().map(gbl->gbl.getGblNames()).collect(Collectors.toList());
        System.out.println(allGbls);
        Map<String,Map<String,Integer>> gblDataSkilllevels=new HashMap<>();
        List<Map<String, Map<String,Integer>>> getGblDatasSkilllevels=new ArrayList<>();
        for(String gbl:allGbls){
            Map<String,Integer> gblData=userSkillsService.getTheGETskillsofEmpforGbl(gbl,batch);
            gblDataSkilllevels.put(gbl,gblData);
        }
        getGblDatasSkilllevels.add(gblDataSkilllevels);
        return getGblDatasSkilllevels;
    }

}
