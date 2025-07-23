package com.redmath.GymManagementApp.config;

import com.redmath.GymManagementApp.admin.AdminRepository;
import com.redmath.GymManagementApp.member.MemberRepository;
import com.redmath.GymManagementApp.trainer.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AdminRepository adminRepo;

    @Autowired
    private TrainerRepository trainerRepo;

    @Autowired
    private MemberRepository memberRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var adminOpt = adminRepo.findByUsername(username);
        if (adminOpt.isPresent()) {
            var admin = adminOpt.get();
            return new User(admin.getUsername(), admin.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + admin.getRole())));
        }

        var trainerOpt = trainerRepo.findByUsername(username);
        if (trainerOpt.isPresent()) {
            var trainer = trainerOpt.get();
            return new User(trainer.getUsername(), trainer.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + trainer.getRole())));
        }

        var memberOpt = memberRepo.findByUsername(username);
        if (memberOpt.isPresent()) {
            var member = memberOpt.get();
            return new User(member.getUsername(), member.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole())));
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}
