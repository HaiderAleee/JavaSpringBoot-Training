package com.redmath.GymManagementApp.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmath.GymManagementApp.config.CustomUserDetailsService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private Admin createTestAdmin(Long id) {
        Admin admin = new Admin();
        admin.setId(id);
        admin.setUsername("admin" + id);
        admin.setPassword("password" + id);
        admin.setRole("ADMIN");
        return admin;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllAdmins_WithAdminRole_ShouldSucceed() throws Exception {

        Admin admin1 = createTestAdmin(1L);
        Admin admin2 = createTestAdmin(2L);
        List<Admin> admins = Arrays.asList(admin1, admin2);

        Mockito.when(adminService.getAllAdmins()).thenReturn(admins);


        mockMvc.perform(get("/admins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is(admin1.getUsername())))
                .andExpect(jsonPath("$[1].username", is(admin2.getUsername())));
    }

    @Test
    @WithMockUser(roles = "MEMBER") // Non-admin role
    public void testGetAllAdmins_WithNonAdminRole_ShouldForbid() throws Exception {

        mockMvc.perform(get("/admins"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetAllAdmins_Unauthenticated_ShouldUnauthorize() throws Exception {
        mockMvc.perform(get("/admins"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateAdmin_WithAdminRole_ShouldSucceed() throws Exception {
        Admin newAdmin = createTestAdmin(null);
        Admin savedAdmin = createTestAdmin(1L);

        Mockito.when(adminService.createAdmin(Mockito.any(Admin.class))).thenReturn(savedAdmin);

        mockMvc.perform(post("/admins")
                        .with(csrf()) // Include CSRF token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedAdmin.getId().intValue())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateAdmin_WithoutCsrf_ShouldForbid() throws Exception {
        Admin newAdmin = createTestAdmin(null);
        mockMvc.perform(post("/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAdmin)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateAdmin_WithAdminRole_ShouldSucceed() throws Exception {
        Long adminId = 1L;
        Admin existingAdmin = createTestAdmin(adminId);
        Admin updatedAdmin = createTestAdmin(adminId);
        updatedAdmin.setUsername("updatedUsername");

        Mockito.when(adminService.updateAdmin(Mockito.eq(adminId), Mockito.any(Admin.class)))
                .thenReturn(updatedAdmin);

        mockMvc.perform(put("/admins/{id}", adminId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("updatedUsername")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteAdmin_WithAdminRole_ShouldSucceed() throws Exception {
        Long adminId = 1L;

        Mockito.doNothing().when(adminService).deleteAdmin(adminId);
        mockMvc.perform(delete("/admins/{id}", adminId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    public void testDeleteAdmin_WithNonAdminRole_ShouldForbid() throws Exception {
        Long adminId = 1L;
        mockMvc.perform(delete("/admins/{id}", adminId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

}

