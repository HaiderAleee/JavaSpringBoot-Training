package com.redmath.GymManagementApp.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AdminService adminService;


    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAdmin() throws Exception {
        Admin admin = new Admin();
        admin.setUsername("admin1");
        when(adminService.createAdmin(any(Admin.class))).thenReturn(admin);
        mockMvc.perform(post("/admins")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(admin)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateAdmin() throws Exception {
        Admin admin = new Admin();
        admin.setId(1L);
        when(adminService.updateAdmin(eq(1L), any(Admin.class))).thenReturn(admin);
        mockMvc.perform(put("/admins/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(admin)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteAdmin() throws Exception {
        doNothing().when(adminService).deleteAdmin(1L);
        mockMvc.perform(delete("/admins/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllAdmins() throws Exception {
        when(adminService.getAllAdmins()).thenReturn(List.of(new Admin()));
        mockMvc.perform(get("/admins"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAdminById() throws Exception {
        Admin admin = new Admin();
        admin.setId(1L);
        when(adminService.getAdminById(1L)).thenReturn(Optional.of(admin));
        mockMvc.perform(get("/admins/1"))
                .andExpect(status().isOk());
    }


}