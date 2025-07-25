package com.redmath.GymManagementApp.member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
@Getter
@Setter
@Entity
public class Member {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private Long trainerid;
    private String username;
    private String password;
    private String phoneNumber;
    private String gender;
    private LocalDate joinDate;
    private String role = "MEMBER";

}
