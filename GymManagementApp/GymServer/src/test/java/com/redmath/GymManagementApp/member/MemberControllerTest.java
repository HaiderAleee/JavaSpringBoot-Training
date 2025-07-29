package com.redmath.GymManagementApp.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmath.GymManagementApp.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@Import(SecurityConfig.class)
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    private Member createTestMember(Long id, Long trainerId) {
        Member member = new Member();
        member.setId(id);
        member.setTrainerid(trainerId);
        member.setUsername("member" + id);
        member.setPassword("password" + id);
        member.setPhoneNumber("1234567890");
        member.setGender("Male");
        member.setJoinDate(LocalDate.now());
        member.setRole("MEMBER");
        return member;
    }

    // Test for GET /members
    @Test
    @WithMockUser(roles = "TRAINER")
    public void testGetAllMembers_WithTrainerRole_ShouldSucceed() throws Exception {
        Member member1 = createTestMember(1L, 1L);
        Member member2 = createTestMember(2L, 1L);
        List<Member> members = Arrays.asList(member1, member2);

        Mockito.when(memberService.getAllMembers()).thenReturn(members);

        mockMvc.perform(get("/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is(member1.getUsername())))
                .andExpect(jsonPath("$[1].username", is(member2.getUsername())));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testGetAllMembers_WithMemberRole_ShouldForbid() throws Exception {
        mockMvc.perform(get("/members"))
                .andExpect(status().isForbidden());
    }

    // Test for GET /members/{id}
    @Test
    @WithMockUser(username = "member1", roles = "MEMBER")
    public void testGetMemberById_OwnProfile_ShouldSucceed() throws Exception {
        Long memberId = 1L;
        Member member = createTestMember(memberId, 1L);

        Mockito.when(memberService.getMemberById(memberId)).thenReturn(Optional.of(member));

        mockMvc.perform(get("/members/{id}", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(member.getUsername())));
    }

    @Test
    @WithMockUser(username = "othermember", roles = "MEMBER")
    public void testGetMemberById_OtherMemberProfile_ShouldForbid() throws Exception {
        Long memberId = 1L;
        Member member = createTestMember(memberId, 1L);

        Mockito.when(memberService.getMemberById(memberId)).thenReturn(Optional.of(member));

        mockMvc.perform(get("/members/{id}", memberId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    public void testGetMemberById_WithTrainerRole_ShouldSucceed() throws Exception {
        Long memberId = 1L;
        Member member = createTestMember(memberId, 1L);

        Mockito.when(memberService.getMemberById(memberId)).thenReturn(Optional.of(member));

        mockMvc.perform(get("/members/{id}", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(member.getUsername())));
    }

    // Test for POST /members
    @Test
    public void testCreateMember_Unauthenticated_ShouldSucceed() throws Exception {
        Member newMember = createTestMember(null, null);
        Member savedMember = createTestMember(1L, 1L);

        Mockito.when(memberService.createMember(Mockito.any(Member.class))).thenReturn(savedMember);

        mockMvc.perform(post("/members")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newMember)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedMember.getId().intValue())));
    }

    // Test for PUT /members/{id}
    @Test
    @WithMockUser(username = "member1", roles = "MEMBER")
    public void testUpdateMember_OwnProfile_ShouldSucceed() throws Exception {
        Long memberId = 1L;
        Member existingMember = createTestMember(memberId, 1L);
        Member updatedMember = createTestMember(memberId, 1L);
        updatedMember.setPhoneNumber("9876543210");

        Mockito.when(memberService.updateMember(Mockito.eq(memberId), Mockito.any(Member.class)))
                .thenReturn(updatedMember);

        mockMvc.perform(put("/members/{id}", memberId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMember)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber", is("9876543210")));
    }

    @Test
    @WithMockUser(username = "othermember", roles = "MEMBER")
    public void testUpdateMember_OtherMemberProfile_ShouldForbid() throws Exception {
        Long memberId = 1L;
        Member updatedMember = createTestMember(memberId, 1L);

        mockMvc.perform(put("/members/{id}", memberId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMember)))
                .andExpect(status().isForbidden());
    }

    // Test for DELETE /members/{id}
    @Test
    @WithMockUser(roles = "TRAINER")
    public void testDeleteMember_WithTrainerRole_ShouldSucceed() throws Exception {
        Long memberId = 1L;

        Mockito.doNothing().when(memberService).deleteMember(memberId);

        mockMvc.perform(delete("/members/{id}", memberId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testDeleteMember_WithMemberRole_ShouldForbid() throws Exception {
        Long memberId = 1L;

        mockMvc.perform(delete("/members/{id}", memberId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // Test for GET /members/by-trainer/{trainerId}
    @Test
    @WithMockUser(roles = "TRAINER")
    public void testGetMembersByTrainerId_WithTrainerRole_ShouldSucceed() throws Exception {
        Long trainerId = 1L;
        Member member1 = createTestMember(1L, trainerId);
        Member member2 = createTestMember(2L, trainerId);
        List<Member> members = Arrays.asList(member1, member2);

        Mockito.when(memberService.getMembersByTrainerId(trainerId)).thenReturn(members);

        mockMvc.perform(get("/members/by-trainer/{trainerId}", trainerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].trainerid", is(trainerId.intValue())))
                .andExpect(jsonPath("$[1].trainerid", is(trainerId.intValue())));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testGetMembersByTrainerId_WithMemberRole_ShouldForbid() throws Exception {
        Long trainerId = 1L;

        mockMvc.perform(get("/members/by-trainer/{trainerId}", trainerId))
                .andExpect(status().isForbidden());
    }

    // Test for GET /members/me
    @Test
    @WithMockUser(username = "testuser", roles = "MEMBER")
    public void testGetMyProfile_AuthenticatedMember_ShouldSucceed() throws Exception {
        Member member = createTestMember(1L, 1L);
        member.setUsername("testuser");

        Mockito.when(memberService.getMemberByUsername("testuser")).thenReturn(Optional.of(member));

        mockMvc.perform(get("/members/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")));
    }

    @Test
    public void testGetMyProfile_Unauthenticated_ShouldUnauthorize() throws Exception {
        mockMvc.perform(get("/members/me"))
                .andExpect(status().isUnauthorized());
    }
}