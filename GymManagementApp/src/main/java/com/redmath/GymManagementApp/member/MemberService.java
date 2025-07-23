package com.redmath.GymManagementApp.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepo;

    public List<Member> getAllMembers() {
        return memberRepo.findAll();
    }

    public Optional<Member> getMemberById(Long id) {
        return memberRepo.findById(id);
    }

    public Member createMember(Member member) {
        return memberRepo.save(member);
    }

    public Member updateMember(Long id, Member updatedMember) {
        updatedMember.setId(id);
        return memberRepo.save(updatedMember);
    }

    public void deleteMember(Long id) {
        memberRepo.deleteById(id);
    }
}
