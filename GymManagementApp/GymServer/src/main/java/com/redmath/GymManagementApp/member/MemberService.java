package com.redmath.GymManagementApp.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Member> getAllMembers() {
        return memberRepo.findAll();
    }

    public Optional<Member> getMemberById(Long id) {
        return memberRepo.findById(id);
    }

    public Member createMember(Member member) {
        member.setRole("MEMBER");
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        return memberRepo.save(member);
    }

    public Member updateMember(Long id, Member updatedMember) {
        updatedMember.setId(id);
        updatedMember.setRole("MEMBER");

        if (updatedMember.getPassword() != null && !updatedMember.getPassword().isEmpty()) {
            updatedMember.setPassword(passwordEncoder.encode(updatedMember.getPassword()));
        } else {
            Member existingMember = memberRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Member not found"));
            updatedMember.setPassword(existingMember.getPassword()); // Keep old password
        }

        return memberRepo.save(updatedMember);
    }


    public List<Member> getMembersByTrainerId(Long trainerId) {
        return memberRepo.findByTrainerid(trainerId);
    }


    public void deleteMember(Long id) {
        memberRepo.deleteById(id);
    }

    public Optional<Member> getMemberByUsername(String username) {
        return memberRepo.findByUsername(username);
    }

    public boolean trainerExists(Long trainerId) {
        return memberRepo.existsByTrainerid(trainerId);
    }
}
