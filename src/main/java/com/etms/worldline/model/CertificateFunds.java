package com.etms.worldline.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
public class CertificateFunds {
   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   private double totalFunds=1200000.00;
}
