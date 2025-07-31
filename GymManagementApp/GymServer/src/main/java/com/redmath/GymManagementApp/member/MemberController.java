package com.redmath.GymManagementApp.member;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<Member> getAllMembers() {
        return memberService.getAllMembers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        return memberService.getMemberById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Member> createMember(@RequestBody @Valid Member member) {
        return ResponseEntity.ok(memberService.createMember(member));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody @Valid Member member) {
        return ResponseEntity.ok(memberService.updateMember(id, member));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-trainer/{trainerId}")
    public List<Member> getMembersByTrainerId(@PathVariable Long trainerId) {
        return memberService.getMembersByTrainerId(trainerId);
    }

    @GetMapping("/me")
    public ResponseEntity<Member> getMyProfile(Authentication authentication) {
        return memberService.getMemberByUsername(authentication.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<Member> completeProfile(
            @RequestBody @Valid MemberProfileCompletionDTO profileData,
            @AuthenticationPrincipal Jwt jwt) {

        Member member = memberService.getMemberByUsername(jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (profileData.getTrainerId() != null && !memberService.trainerExists(profileData.getTrainerId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid trainer ID");
        }

        member.setPhoneNumber(profileData.getPhoneNumber());
        member.setTrainerid(profileData.getTrainerId());
        member.setGender(profileData.getGender());

        return ResponseEntity.ok(memberService.updateMember(member.getId(), member));
    }
}