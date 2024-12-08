package com.etms.worldline.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@RequiredArgsConstructor
@Table(name = "user_certifications")
public class UserCertifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cert_id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonManagedReference
    private User user;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "certificate_id")
    @JsonManagedReference
    private CertificateMaster cert;

    private String cert_url;

    private String cert_file_path;

    private String cert_status;

    @PrePersist
    protected void OnCreate(){
        createdAt=LocalDateTime.now();
    }

    public UserCertifications(User user, CertificateMaster cert, String cert_url, String cert_file_path,String cert_status) {
        this.user = user;
        this.cert = cert;
        this.cert_url=cert_url;
        this.cert_file_path = cert_file_path;
        this.cert_status=cert_status;
    }

    public UserCertifications(CertificateMaster cert, String cert_file_path) {
        this.cert = cert;
        this.cert_file_path = cert_file_path;
    }
}
