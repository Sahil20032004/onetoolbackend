package com.etms.worldline.Repository;

import com.etms.worldline.model.CertificateMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CertificateMasterRepository extends JpaRepository<CertificateMaster,Long> {
}
