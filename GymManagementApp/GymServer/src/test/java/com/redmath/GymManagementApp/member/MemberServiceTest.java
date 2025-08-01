package com.redmath.GymManagementApp.member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberServiceTest {

    @Mock
    private MemberRepository memberRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllMembers() {
        when(memberRepo.findAll()).thenReturn(List.of(new Member()));
        List<Member> members = memberService.getAllMembers();
        assertFalse(members.isEmpty());
    }

    @Test
    void testGetMemberById() {
        Member member = new Member();
        member.setId(1L);
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        Optional<Member> result = memberService.getMemberById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void testCreateMember() {
        Member member = new Member();
        member.setPassword("raw");
        when(passwordEncoder.encode("raw")).thenReturn("encoded");
        when(memberRepo.save(any(Member.class))).thenReturn(member);
        Member result = memberService.createMember(member);
        assertEquals("MEMBER", result.getRole());
        assertEquals("encoded", result.getPassword());
    }

    @Test
    void testUpdateMember_WithPassword() {
        Member updated = new Member();
        updated.setPassword("newpass");
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");
        when(memberRepo.save(any(Member.class))).thenReturn(updated);
        Member result = memberService.updateMember(1L, updated);
        assertEquals("MEMBER", result.getRole());
        assertEquals("encoded", result.getPassword());
        assertEquals(1L, result.getId());
    }

    @Test
    void testUpdateMember_WithoutPassword() {
        Member updated = new Member();
        updated.setPassword("");
        Member existing = new Member();
        existing.setPassword("oldpass");
        when(memberRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(memberRepo.save(any(Member.class))).thenReturn(updated);
        Member result = memberService.updateMember(1L, updated);
        assertEquals("MEMBER", result.getRole());
        assertEquals("oldpass", result.getPassword());
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetMembersByTrainerId() {
        when(memberRepo.findByTrainerid(2L)).thenReturn(List.of(new Member()));
        List<Member> members = memberService.getMembersByTrainerId(2L);
        assertFalse(members.isEmpty());
    }

    @Test
    void testDeleteMember() {
        doNothing().when(memberRepo).deleteById(1L);
        memberService.deleteMember(1L);
        verify(memberRepo, times(1)).deleteById(1L);
    }

    @Test
    void testGetMemberByUsername() {
        Member member = new Member();
        member.setUsername("user");
        when(memberRepo.findByUsername("user")).thenReturn(Optional.of(member));
        Optional<Member> result = memberService.getMemberByUsername("user");
        assertTrue(result.isPresent());
        assertEquals("user", result.get().getUsername());
    }

    @Test
    void testTrainerExists() {
        when(memberRepo.existsByTrainerid(5L)).thenReturn(true);
        assertTrue(memberService.trainerExists(5L));
    }
}