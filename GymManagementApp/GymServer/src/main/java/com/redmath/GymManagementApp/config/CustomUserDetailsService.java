package com.redmath.GymManagementApp.config;

import com.redmath.GymManagementApp.admin.AdminRepository;
import com.redmath.GymManagementApp.member.MemberRepository;
import com.redmath.GymManagementApp.trainer.TrainerRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final TrainerRepository trainerRepository;
    private final MemberRepository memberRepository;

    public CustomUserDetailsService(AdminRepository adminRepository, TrainerRepository trainerRepository, MemberRepository memberRepository) {
        this.adminRepository = adminRepository;
        this.trainerRepository = trainerRepository;
        this.memberRepository = memberRepository;
    }

    private User createUserDetails(String username, String password, String role) {
        return new User(username, password, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var adminOptional = adminRepository.findByUsername(username)
                .map(admin -> createUserDetails(admin.getUsername(), admin.getPassword(), admin.getRole()));
        if (adminOptional.isPresent()) return adminOptional.get();

        var trainerOptional = trainerRepository.findByUsername(username)
                .map(trainer -> createUserDetails(trainer.getUsername(), trainer.getPassword(), trainer.getRole()));
        if (trainerOptional.isPresent()) return trainerOptional.get();

        var memberOptional = memberRepository.findByUsername(username)
                .map(member -> createUserDetails(member.getUsername(), member.getPassword(), member.getRole()));
        if (memberOptional.isPresent()) return memberOptional.get();

        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}
