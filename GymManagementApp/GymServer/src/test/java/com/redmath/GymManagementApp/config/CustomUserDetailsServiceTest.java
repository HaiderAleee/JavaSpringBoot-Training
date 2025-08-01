package com.redmath.GymManagementApp.config;

import com.redmath.GymManagementApp.admin.Admin;
import com.redmath.GymManagementApp.admin.AdminRepository;
import com.redmath.GymManagementApp.member.Member;
import com.redmath.GymManagementApp.member.MemberRepository;
import com.redmath.GymManagementApp.trainer.Trainer;
import com.redmath.GymManagementApp.trainer.TrainerRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    @Mock
    private AdminRepository adminRepo;
    @Mock
    private TrainerRepository trainerRepo;
    @Mock
    private MemberRepository memberRepo;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    public CustomUserDetailsServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_adminFound() {
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword("pass");
        admin.setRole("ADMIN");
        when(adminRepo.findByUsername("admin")).thenReturn(Optional.of(admin));

        UserDetails user = userDetailsService.loadUserByUsername("admin");
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_trainerFound() {
        when(adminRepo.findByUsername("trainer")).thenReturn(Optional.empty());
        Trainer trainer = new Trainer();
        trainer.setUsername("trainer");
        trainer.setPassword("pass");
        trainer.setRole("TRAINER");
        when(trainerRepo.findByUsername("trainer")).thenReturn(Optional.of(trainer));

        UserDetails user = userDetailsService.loadUserByUsername("trainer");
        assertThat(user.getUsername()).isEqualTo("trainer");
        assertThat(user.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_TRAINER");
    }

    @Test
    void loadUserByUsername_memberFound() {
        when(adminRepo.findByUsername("member")).thenReturn(Optional.empty());
        when(trainerRepo.findByUsername("member")).thenReturn(Optional.empty());
        Member member = new Member();
        member.setUsername("member");
        member.setPassword("pass");
        member.setRole("MEMBER");
        when(memberRepo.findByUsername("member")).thenReturn(Optional.of(member));

        UserDetails user = userDetailsService.loadUserByUsername("member");
        assertThat(user.getUsername()).isEqualTo("member");
        assertThat(user.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_MEMBER");
    }

    @Test
    void loadUserByUsername_notFound() {
        when(adminRepo.findByUsername("unknown")).thenReturn(Optional.empty());
        when(trainerRepo.findByUsername("unknown")).thenReturn(Optional.empty());
        when(memberRepo.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}