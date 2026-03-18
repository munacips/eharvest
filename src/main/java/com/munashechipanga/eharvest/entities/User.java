package com.munashechipanga.eharvest.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nationalId;
    private String firstName;
    private String lastName;
    private String username;
    private String role;

    @Column(unique = true)
    private String email;
    private String password;

    @Column(unique = true)
    private String phoneNumber;
    private String address;
    private Boolean active;
    private Boolean verified;
    private Integer trustScore;

    //escrow
    private Double usdBalance;
    private Double zigBalance;
}
