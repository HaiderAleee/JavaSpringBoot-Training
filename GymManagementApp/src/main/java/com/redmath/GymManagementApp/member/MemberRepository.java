package com.redmath.GymManagementApp.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.config.annotation.web.PortMapperDsl;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
}