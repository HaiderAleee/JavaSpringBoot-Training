package com.redmath.GymManagementApp.admin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.service.annotation.GetExchange;

@Slf4j
@Entity
@Getter
@Setter
public class Admin {

    @Id
    private Long id;
    private String username;
    private String password;
    private String role = "ADMIN";
}
