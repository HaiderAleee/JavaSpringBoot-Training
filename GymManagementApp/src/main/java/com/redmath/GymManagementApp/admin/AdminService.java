package com.redmath.GymManagementApp.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepo;

    public List<Admin> getAllAdmins() {
        return adminRepo.findAll();
    }

    public Optional<Admin> getAdminById(Long id) {
        return adminRepo.findById(id);
    }

    public Admin createAdmin(Admin admin) {
        return adminRepo.save(admin);
    }

    public Admin updateAdmin(Long id, Admin updatedAdmin) {
        updatedAdmin.setId(id);
        return adminRepo.save(updatedAdmin);
    }

    public void deleteAdmin(Long id) {
        adminRepo.deleteById(id);
    }
}
