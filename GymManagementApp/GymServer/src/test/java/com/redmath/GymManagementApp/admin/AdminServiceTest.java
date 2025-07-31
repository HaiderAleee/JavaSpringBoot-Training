package com.redmath.GymManagementApp.admin;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    @Mock
    private AdminRepository adminRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    public AdminServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllAdmins() {
        when(adminRepo.findAll()).thenReturn(List.of(new Admin()));
        List<Admin> admins = adminService.getAllAdmins();
        assertFalse(admins.isEmpty());
    }

    @Test
    void testGetAdminById() {
        Admin admin = new Admin();
        admin.setId(1L);
        when(adminRepo.findById(1L)).thenReturn(Optional.of(admin));
        Optional<Admin> result = adminService.getAdminById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void testCreateAdmin() {
        Admin admin = new Admin();
        admin.setPassword("raw");
        when(passwordEncoder.encode("raw")).thenReturn("encoded");
        when(adminRepo.save(any(Admin.class))).thenReturn(admin);
        Admin result = adminService.createAdmin(admin);
        assertEquals("ADMIN", result.getRole());
        assertEquals("encoded", result.getPassword());
    }

    @Test
    void testUpdateAdmin() {
        Admin updated = new Admin();
        updated.setPassword("newpass");
        Admin existing = new Admin();
        existing.setPassword("oldpass");
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");
        when(adminRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(adminRepo.save(any(Admin.class))).thenReturn(updated);
        Admin result = adminService.updateAdmin(1L, updated);
        assertEquals("ADMIN", result.getRole());
        assertEquals("encoded", result.getPassword());
    }

    @Test
    void testDeleteAdmin() {
        doNothing().when(adminRepo).deleteById(1L);
        adminService.deleteAdmin(1L);
        verify(adminRepo, times(1)).deleteById(1L);
    }
}