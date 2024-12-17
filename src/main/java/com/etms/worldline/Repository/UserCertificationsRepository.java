package com.etms.worldline.Repository;

import com.etms.worldline.model.UserCertifications;
import com.etms.worldline.payload.response.CertificationsResponse;
import com.etms.worldline.payload.response.FetchUserCertificates;
import com.etms.worldline.payload.response.UserCertdetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

public interface UserCertificationsRepository extends JpaRepository<UserCertifications,Long> {
    @Query("SELECT new com.etms.worldline.payload.response.FetchUserCertificates(c.cert.certificate_name,c.cert_url,c.cert_file_path) FROM UserCertifications c WHERE c.user.user_id=:user_id")
    List<FetchUserCertificates> getUserCertificatesdetails(@Param("user_id") Long user_id);
    @Query("SELECT c FROM UserCertifications c WHERE c.cert.certificate_name=:cert_name")
    Optional<UserCertifications> findByCert_name(@Param("cert_name") String cert_name);
    @Query("SELECT new com.etms.worldline.payload.response.CertificationsResponse(c.cert.certificate_name,count(c.user.user_id)) from UserCertifications c group by c.cert.certificate_name order by count(c.user.user_id) desc")
     List<CertificationsResponse> getUserCertificatesCount();
    @Query("SELECT new com.etms.worldline.payload.response.CertificationsResponse(c.cert.certificate_name,count(c.user.user_id)) from UserCertifications c WHERE c.user.manager_id=:manager_id group by c.cert.certificate_name order by count(c.user.user_id) desc")
    List<CertificationsResponse> getReportUserCertificatesCount(@Param("manager_id") Long manager_id);
    @Query("SELECT SUM(c.cert.amount_allocated) FROM UserCertifications c WHERE c.cert_status='completed'")
    double getUsedFunds();
    @Query("SELECT SUM(c.cert.amount_allocated) FROM UserCertifications c")
    double getUsedandPendingFunds();
    @Query("SELECT COUNT(c) FROM UserCertifications c WHERE c.cert_status='completed'")
    int getCompletedMembers();
    @Query("SELECT COUNT(c) FROM UserCertifications c")
    int getAllMembers();
    @Query("SELECT new com.etms.worldline.payload.response.UserCertdetails(c.user.user_id,c.user.username,c.user.gbl) from UserCertifications c WHERE c.cert.certificate_name=:certificate_name AND c.cert_status='completed'")
    List<UserCertdetails> getCertUserDetails(@Param("certificate_name") String certificate_name);
}
