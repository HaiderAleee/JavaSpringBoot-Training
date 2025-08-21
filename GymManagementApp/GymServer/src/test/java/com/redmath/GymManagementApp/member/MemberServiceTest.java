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
    void testGetMemberById_NotFound() {
        when(memberRepo.findById(99L)).thenReturn(Optional.empty());
        Optional<Member> result = memberService.getMemberById(99L);
        assertTrue(result.isEmpty());
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
    void testUpdateMember_WithProfileCompletion() {
        Member existing = new Member();
        existing.setId(1L);
        existing.setRole("OLD_ROLE");

        when(memberRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(memberRepo.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        MemberProfileCompletionDTO dto = new MemberProfileCompletionDTO();
        dto.setPhoneNumber("1234567890");
        dto.setTrainerid(42L);
        dto.setGender("Male");

        Member result = memberService.updateMember(1L, dto);

        assertEquals(1L, result.getId());
        assertEquals("1234567890", result.getPhoneNumber());
        assertEquals(42L, result.getTrainerid());
        assertEquals("Male", result.getGender());
        assertEquals("MEMBER", result.getRole());

        verify(memberRepo).findById(1L);
        verify(memberRepo).save(existing);
    }

    @Test
    void testUpdateMember_WithoutProfileFields() {
        Member existing = new Member();
        existing.setId(1L);
        existing.setPhoneNumber("oldPhone");
        existing.setTrainerid(99L);
        existing.setGender("Female");
        existing.setRole("OLD_ROLE");

        when(memberRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(memberRepo.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        MemberProfileCompletionDTO dto = new MemberProfileCompletionDTO();

        Member result = memberService.updateMember(1L, dto);

        assertEquals(1L, result.getId());
        assertEquals("oldPhone", result.getPhoneNumber());
        assertEquals(99L, result.getTrainerid());
        assertEquals("Female", result.getGender());
        assertEquals("MEMBER", result.getRole());

        verify(memberRepo).findById(1L);
        verify(memberRepo).save(existing);
    }

    @Test
    void testUpdateMember_NotFound() {
        when(memberRepo.findById(777L)).thenReturn(Optional.empty());
        MemberProfileCompletionDTO dto = new MemberProfileCompletionDTO();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> memberService.updateMember(777L, dto));
        assertEquals("Member not found", ex.getMessage());
        verify(memberRepo).findById(777L);
        verify(memberRepo, never()).save(any());
    }

    @Test
    void testUpdatePassword_Success() {
        Member member = new Member();
        member.setId(1L);
        member.setPassword("old");
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.encode("newpass")).thenReturn("encodedNew");
        when(memberRepo.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        memberService.updatePassword(1L, "newpass");

        assertEquals("encodedNew", member.getPassword());
        verify(memberRepo).findById(1L);
        verify(passwordEncoder).encode("newpass");
        verify(memberRepo).save(member);
    }

    @Test
    void testUpdatePassword_NotFound() {
        when(memberRepo.findById(123L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> memberService.updatePassword(123L, "x"));
        assertEquals("Member not found", ex.getMessage());
        verify(memberRepo).findById(123L);
        verify(memberRepo, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
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
