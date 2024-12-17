package com.etms.worldline.Service;

import com.etms.worldline.Repository.CertificateMasterRepository;
import com.etms.worldline.Repository.CerttotalFundsRepo;
import com.etms.worldline.Repository.UserCertificationsRepository;
import com.etms.worldline.model.CertificateFunds;
import com.etms.worldline.model.CertificateMaster;
import com.etms.worldline.payload.response.MessageResponse;
import com.etms.worldline.payload.response.UserCertdetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CertificateMasterService {
      @Autowired
      private CertificateMasterRepository certificateMasterRepository;
      @Autowired
      private UserCertificationsRepository userCertificationsRepository;
      @Autowired
      private CerttotalFundsRepo certtotalFundsRepo;
      public ResponseEntity<?> saveCertificate(CertificateMaster certificateMaster){
          try{
//              CertificateFunds totalFunds=certtotalFundsRepo.getById(1L);
//              if(certificateMaster.getAmount_allocated()>=totalFunds.getTotalFunds()){
//                  return ResponseEntity.ok(new MessageResponse(40000,"Insufficient Funds"));
//              }
//              totalFunds.setTotalFunds(totalFunds.getTotalFunds()-certificateMaster.getAmount_allocated());
//              certtotalFundsRepo.save(totalFunds);
              certificateMasterRepository.save(certificateMaster);
              return ResponseEntity.ok(new MessageResponse(20001,"Certificate Details Saved Successfully"));
          }
          catch (Exception e){
              return ResponseEntity.ok(new MessageResponse(50000,"Failed in saving certificate details"));
          }
      }
    public ResponseEntity<?> updateCertificate(CertificateMaster certificateMaster){
        try{
//            CertificateFunds totalFunds=certtotalFundsRepo.getById(1L);
       CertificateMaster certificateMaster1=certificateMasterRepository.getById(certificateMaster.getId());
//            if(certificateMaster.getAmount_allocated()>=totalFunds.getTotalFunds()){
//                return ResponseEntity.ok(new MessageResponse(40000,"Insufficient Funds"));
//            }
//            totalFunds.setTotalFunds(totalFunds.getTotalFunds()-certificateMaster.getAmount_allocated());
            certificateMaster1.setCertificate_name(certificateMaster.getCertificate_name());
            certificateMaster1.setAmount_allocated(certificateMaster.getAmount_allocated());
//            certtotalFundsRepo.save(totalFunds);
            certificateMasterRepository.save(certificateMaster1);
            return ResponseEntity.ok(new MessageResponse(20001,"Certificate Details Saved Successfully"));
        }
        catch (Exception e){
            return ResponseEntity.ok(new MessageResponse(50000,"Failed in saving certificate details"));
        }
    }
    public ResponseEntity<?> deleteCertificate(Long cert_id){
        try{
//            CertificateFunds totalFunds=certtotalFundsRepo.getById(1L);
//
//            totalFunds.setTotalFunds(totalFunds.getTotalFunds()+certificateMaster.getAmount_allocated());
//            certtotalFundsRepo.save(totalFunds);
            certificateMasterRepository.deleteById(cert_id);
            return ResponseEntity.ok(new MessageResponse(20001,"Certificate Details deleted Successfully"));
        }
        catch (Exception e){
            return ResponseEntity.ok(new MessageResponse(50000,"Failed in deleting certificate details"));
        }
    }
    public List<CertificateMaster> getCertificatesfromMaster() {
        System.out.println(userCertificationsRepository.getUsedFunds());
        return certificateMasterRepository.findAll();
    }
    public List<Integer> getCertDashDetails(){
          List<Integer> dashDetails=new ArrayList<>();
          double totalFunds=certtotalFundsRepo.getById(1L).getTotalFunds();
          dashDetails.add((int)totalFunds);
          dashDetails.add((int) userCertificationsRepository.getUsedandPendingFunds());
          dashDetails.add((int)userCertificationsRepository.getUsedFunds());
          dashDetails.add(userCertificationsRepository.getAllMembers());
          dashDetails.add(userCertificationsRepository.getCompletedMembers());
          return dashDetails;
      }
      public List<UserCertdetails> getUserCertDetails(String certificate_name){
          return userCertificationsRepository.getCertUserDetails(certificate_name);
      }
}
