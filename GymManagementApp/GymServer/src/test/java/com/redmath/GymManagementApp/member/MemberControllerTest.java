package com.redmath.GymManagementApp.member;

import com.redmath.GymManagementApp.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@TestPropertySource(properties = "jwt.signing.key=test-signing-key-1234567890123456")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private MemberRepository memberRepository;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        jwtToken = generateJwt("admin", "ROLE_ADMIN");
    }

    private String generateJwt(String username, String role) {
        byte[] key = "test-signing-key-1234567890123456".getBytes();
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
    }

    private String bearerToken() {
        return "Bearer " + jwtToken;
    }

    @Test
    void testGetAllMembers() throws Exception {
        when(memberService.getAllMembers()).thenReturn(List.of(new Member()));

        mockMvc.perform(get("/api/members")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMyProfile_Found() throws Exception {
        Member member = new Member();
        member.setUsername("admin");
        when(memberService.getMemberByUsername("admin")).thenReturn(Optional.of(member));

        mockMvc.perform(get("/api/members/me")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMyProfile_NotFound() throws Exception {
        when(memberService.getMemberByUsername("admin")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/members/me")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCompleteProfile_Success() throws Exception {
        Member member = new Member();
        member.setId(1L);
        member.setUsername("admin");

        when(memberService.getMemberByUsername("admin")).thenReturn(Optional.of(member));
        when(memberService.trainerExists(42L)).thenReturn(true);
        when(memberService.updateMember(eq(1L), any(MemberProfileCompletionDTO.class))).thenReturn(member);

        String json = "{\"phoneNumber\":\"1234567890\",\"trainerid\":42,\"gender\":\"Male\"}";

        mockMvc.perform(put("/api/members/me")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void testCompleteProfile_UserNotFound() throws Exception {
        when(memberService.getMemberByUsername("admin")).thenReturn(Optional.empty());

        String json = "{\"phoneNumber\":\"1234567890\",\"trainerid\":42,\"gender\":\"Male\"}";

        mockMvc.perform(put("/api/members/me")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetMemberById() throws Exception {
        Member member = new Member();
        member.setId(1L);
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(member));

        mockMvc.perform(get("/api/members/1")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateMember() throws Exception {
        Member member = new Member();
        member.setUsername("member");
        member.setPassword("pass");
        when(memberService.createMember(any(Member.class))).thenReturn(member);

        mockMvc.perform(post("/api/members")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"member\",\"password\":\"pass\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateMember() throws Exception {
        Member member = new Member();
        member.setId(1L);
        when(memberService.updateMember(eq(1L), any(MemberProfileCompletionDTO.class))).thenReturn(member);

        mockMvc.perform(put("/api/members/1")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"member\",\"password\":\"pass\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteMember() throws Exception {
        doNothing().when(memberService).deleteMember(1L);

        mockMvc.perform(delete("/api/members/1")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetMembersByTrainerId() throws Exception {
        when(memberService.getMembersByTrainerId(1L)).thenReturn(List.of(new Member()));

        mockMvc.perform(get("/api/members/by-trainer/1")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk());
    }
}
