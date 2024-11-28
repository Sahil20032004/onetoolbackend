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

    private String cert_name;

    private String cert_url;

    private String cert_file_path;

    @PrePersist
    protected void OnCreate(){
        createdAt=LocalDateTime.now();
    }

    public UserCertifications(User user, String cert_name, String cert_url, String cert_file_path) {
        this.user = user;
        this.cert_name = cert_name;
        this.cert_url=cert_url;
        this.cert_file_path = cert_file_path;
    }

    public UserCertifications(String cert_name, String cert_file_path) {
        this.cert_name = cert_name;
        this.cert_file_path = cert_file_path;
    }
}
