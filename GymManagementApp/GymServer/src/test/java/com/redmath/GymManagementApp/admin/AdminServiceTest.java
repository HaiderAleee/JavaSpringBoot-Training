package com.redmath.GymManagementApp.admin;

import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void init() {
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
    void testGetAdminById_NotFound() {
        when(adminRepo.findById(9L)).thenReturn(Optional.empty());
        Optional<Admin> result = adminService.getAdminById(9L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateAdmin() {
        Admin admin = new Admin();
        admin.setPassword("raw");
        when(passwordEncoder.encode("raw")).thenReturn("encoded");
        when(adminRepo.save(any(Admin.class))).thenAnswer(inv -> inv.getArgument(0));

        Admin result = adminService.createAdmin(admin);

        assertEquals("ADMIN", result.getRole());
        assertEquals("encoded", result.getPassword());
    }

    @Test
    void testUpdateAdmin_WithPasswordProvided() {
        Admin updated = new Admin();
        updated.setPassword("newpass");

        when(passwordEncoder.encode("newpass")).thenReturn("encodedNew");
        when(adminRepo.findById(1L)).thenReturn(Optional.of(new Admin()));
        when(adminRepo.save(any(Admin.class))).thenAnswer(inv -> inv.getArgument(0));

        Admin result = adminService.updateAdmin(1L, updated);

        assertEquals(1L, result.getId());
        assertEquals("ADMIN", result.getRole());
        assertEquals("encodedNew", result.getPassword());
        verify(passwordEncoder).encode("newpass");
        verify(adminRepo).save(updated);
    }

    @Test
    void testUpdateAdmin_WithoutPassword_UsesExistingPassword() {
        Admin updated = new Admin(); // no password
        Admin existing = new Admin();
        existing.setPassword("keptHash");

        when(adminRepo.findById(2L)).thenReturn(Optional.of(existing));
        when(adminRepo.save(any(Admin.class))).thenAnswer(inv -> inv.getArgument(0));

        Admin result = adminService.updateAdmin(2L, updated);

        assertEquals(2L, result.getId());
        assertEquals("ADMIN", result.getRole());
        assertEquals("keptHash", result.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(adminRepo).save(updated);
    }

    @Test
    void testUpdateAdmin_NotFound_WhenPasswordMissing() {
        Admin updated = new Admin(); // no password
        when(adminRepo.findById(3L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> adminService.updateAdmin(3L, updated));

        assertEquals("Admin not found", ex.getMessage());
        verify(adminRepo, never()).save(any());
    }

    @Test
    void testDeleteAdmin() {
        doNothing().when(adminRepo).deleteById(1L);
        adminService.deleteAdmin(1L);
        verify(adminRepo, times(1)).deleteById(1L);
    }
}
