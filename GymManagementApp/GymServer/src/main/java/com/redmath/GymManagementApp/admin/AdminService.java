package com.redmath.GymManagementApp.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;



    public List<Admin> getAllAdmins() {
        return adminRepo.findAll();
    }

    public Optional<Admin> getAdminById(Long id) {
        return adminRepo.findById(id);
    }

    public Admin createAdmin(Admin admin) {
        admin.setRole("ADMIN");
        String rawPassword = admin.getPassword();
        admin.setPassword(passwordEncoder.encode(rawPassword));
        return adminRepo.save(admin);
    }


    public Admin updateAdmin(Long id, Admin updatedAdmin) {
        updatedAdmin.setId(id);
        updatedAdmin.setRole("ADMIN");

        if (updatedAdmin.getPassword() != null && !updatedAdmin.getPassword().isEmpty()) {
            updatedAdmin.setPassword(passwordEncoder.encode(updatedAdmin.getPassword()));
        } else {
            Admin existingAdmin = adminRepo.findById(id).orElseThrow(() -> new RuntimeException("Admin not found"));
            updatedAdmin.setPassword(existingAdmin.getPassword());
        }
        return adminRepo.save(updatedAdmin);
    }


    public void deleteAdmin(Long id) {
        adminRepo.deleteById(id);
    }
}
