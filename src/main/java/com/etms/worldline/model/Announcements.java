package com.etms.worldline.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.jni.Local;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "announcements")
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Announcements {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long an_id;

    @ElementCollection
    @CollectionTable(name = "sos_announcements", joinColumns = @JoinColumn(name = "an_id"))
    @Column(name = "announcements",length=250)
    private List<String> announcements;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private String location;

    private boolean active=true;

    @PrePersist
    protected void OnCreate(){
        createdAt= LocalDateTime.now();
    }
}
