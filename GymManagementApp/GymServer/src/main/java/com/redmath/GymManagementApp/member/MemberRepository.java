package com.redmath.GymManagementApp.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    List<Member> findByTrainerid(Long trainerid);
    boolean existsByTrainerid(Long trainerId);
    boolean existsByUsername(String email);
}