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

    public Member updateMember(Long id, MemberProfileCompletionDTO dto) {
        Member existingMember = memberRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (dto.getPhoneNumber() != null) {
            existingMember.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getTrainerid() != null) {
            existingMember.setTrainerid(dto.getTrainerid());
        }
        if (dto.getGender() != null) {
            existingMember.setGender(dto.getGender());
        }
        existingMember.setRole("MEMBER");

        return memberRepo.save(existingMember);
    }

    public void updatePassword(Long id, String rawPassword) {
        Member member = memberRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.setPassword(passwordEncoder.encode(rawPassword));
        memberRepo.save(member);
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
