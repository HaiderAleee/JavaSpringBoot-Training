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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String phoneNumber;
    private String gender;
    private String membershipType;
    private LocalDate joinDate;
    private String username;
    private String password;
    private String role = "MEMBER";

}
