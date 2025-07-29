package com.redmath.GymManagementApp.trainer;

import com.fasterxml.jackson.databind.ObjectMapper;
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

@WebMvcTest(TrainerController.class)
@Import(SecurityConfig.class)
public class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainerService trainerService;

    @Autowired
    private ObjectMapper objectMapper;

    private Trainer createTestTrainer(Long id) {
        Trainer trainer = new Trainer();
        trainer.setId(id);
        trainer.setUsername("trainer" + id);
        trainer.setPassword("password" + id);
        trainer.setPhoneNumber("1234567890");
        trainer.setSpecialty("Fitness");
        trainer.setSalary(3000.0);
        trainer.setRole("TRAINER");
        return trainer;
    }

    // Test for GET /trainers
    @Test
    @WithMockUser(roles = {"MEMBER", "TRAINER"})
    public void testGetAllTrainers_WithAllowedRoles_ShouldSucceed() throws Exception {
        Trainer trainer1 = createTestTrainer(1L);
        Trainer trainer2 = createTestTrainer(2L);
        List<Trainer> trainers = Arrays.asList(trainer1, trainer2);

        Mockito.when(trainerService.getAllTrainers()).thenReturn(trainers);

        mockMvc.perform(get("/trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is(trainer1.getUsername())))
                .andExpect(jsonPath("$[1].username", is(trainer2.getUsername())));
    }

    @Test
    public void testGetAllTrainers_Unauthenticated_ShouldUnauthorize() throws Exception {
        mockMvc.perform(get("/trainers"))
                .andExpect(status().isUnauthorized());
    }

    // Test for GET /trainers/{id}
    @Test
    @WithMockUser(roles = {"MEMBER", "TRAINER"})
    public void testGetTrainerById_WithAllowedRoles_ShouldSucceed() throws Exception {
        Long trainerId = 1L;
        Trainer trainer = createTestTrainer(trainerId);

        Mockito.when(trainerService.getTrainerById(trainerId)).thenReturn(Optional.of(trainer));

        mockMvc.perform(get("/trainers/{id}", trainerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(trainer.getUsername())));
    }

    @Test
    @WithMockUser(roles = {"MEMBER", "TRAINER"})
    public void testGetTrainerById_NotFound_ShouldReturnNotFound() throws Exception {
        Long trainerId = 99L;

        Mockito.when(trainerService.getTrainerById(trainerId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/trainers/{id}", trainerId))
                .andExpect(status().isNotFound());
    }

    // Test for POST /trainers
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateTrainer_WithAdminRole_ShouldSucceed() throws Exception {
        Trainer newTrainer = createTestTrainer(null);
        Trainer savedTrainer = createTestTrainer(1L);

        Mockito.when(trainerService.createTrainer(Mockito.any(Trainer.class))).thenReturn(savedTrainer);

        mockMvc.perform(post("/trainers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTrainer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedTrainer.getId().intValue())));
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    public void testCreateTrainer_WithNonAdminRole_ShouldForbid() throws Exception {
        Trainer newTrainer = createTestTrainer(null);

        mockMvc.perform(post("/trainers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTrainer)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateTrainer_WithoutCsrf_ShouldForbid() throws Exception {
        Trainer newTrainer = createTestTrainer(null);

        mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTrainer)))
                .andExpect(status().isForbidden());
    }

    // Test for PUT /trainers/{id}
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateTrainer_WithAdminRole_ShouldSucceed() throws Exception {
        Long trainerId = 1L;
        Trainer existingTrainer = createTestTrainer(trainerId);
        Trainer updatedTrainer = createTestTrainer(trainerId);
        updatedTrainer.setSpecialty("Yoga");

        Mockito.when(trainerService.updateTrainer(Mockito.eq(trainerId), Mockito.any(Trainer.class)))
                .thenReturn(updatedTrainer);

        mockMvc.perform(put("/trainers/{id}", trainerId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTrainer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty", is("Yoga")));
    }

//    @Test
//    @WithMockUser(username = "trainer1", roles = "TRAINER")
//    public void testUpdateTrainer_OwnProfile_ShouldSucceed() throws Exception {
//        Long trainerId = 1L;
//        Trainer existingTrainer = createTestTrainer(trainerId);
//        existingTrainer.setUsername("trainer1");
//        Trainer updatedTrainer = createTestTrainer(trainerId);
//        updatedTrainer.setUsername("trainer1");
//        updatedTrainer.setPhoneNumber("9876543210");
//
//        Mockito.when(trainerService.getTrainerById(trainerId)).thenReturn(Optional.of(existingTrainer));
//        Mockito.when(trainerService.updateTrainer(Mockito.eq(trainerId), Mockito.any(Trainer.class)))
//                .thenReturn(updatedTrainer);
//
//        mockMvc.perform(put("/trainers/{id}", trainerId)
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updatedTrainer)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.phoneNumber", is("9876543210")));
//    }

    @Test
    @WithMockUser(username = "trainer2", roles = "TRAINER")
    public void testUpdateTrainer_OtherTrainerProfile_ShouldForbid() throws Exception {
        Long trainerId = 1L;
        Trainer updatedTrainer = createTestTrainer(trainerId);

        mockMvc.perform(put("/trainers/{id}", trainerId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTrainer)))
                .andExpect(status().isForbidden());
    }

    // Test for DELETE /trainers/{id}
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteTrainer_WithAdminRole_ShouldSucceed() throws Exception {
        Long trainerId = 1L;

        Mockito.doNothing().when(trainerService).deleteTrainer(trainerId);

        mockMvc.perform(delete("/trainers/{id}", trainerId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    public void testDeleteTrainer_WithNonAdminRole_ShouldForbid() throws Exception {
        Long trainerId = 1L;

        mockMvc.perform(delete("/trainers/{id}", trainerId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}