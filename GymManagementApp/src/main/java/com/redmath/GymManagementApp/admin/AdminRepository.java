package com.redmath.GymManagementApp.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.config.annotation.web.PortMapperDsl;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);
}
