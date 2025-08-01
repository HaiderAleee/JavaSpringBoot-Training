package com.redmath.GymManagementApp.trainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(TrainerController.class)
class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainerService trainerService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(roles = {"MEMBER"})
    void testGetAllTrainers_AsMember() throws Exception {
        when(trainerService.getAllTrainers()).thenReturn(List.of(new Trainer()));
        mockMvc.perform(get("/trainers"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void testGetAllTrainers_AsTrainer() throws Exception {
        when(trainerService.getAllTrainers()).thenReturn(List.of(new Trainer()));
        mockMvc.perform(get("/trainers"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllTrainers_AsAdmin() throws Exception {
        when(trainerService.getAllTrainers()).thenReturn(List.of(new Trainer()));
        mockMvc.perform(get("/trainers"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void testGetTrainerById_AsTrainer_Found() throws Exception {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        when(trainerService.getTrainerById(1L)).thenReturn(Optional.of(trainer));
        mockMvc.perform(get("/trainers/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetTrainerById_AsAdmin_Found() throws Exception {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        when(trainerService.getTrainerById(1L)).thenReturn(Optional.of(trainer));
        mockMvc.perform(get("/trainers/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void testGetTrainerById_AsTrainer_NotFound() throws Exception {
        when(trainerService.getTrainerById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/trainers/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetTrainerById_AsAdmin_NotFound() throws Exception {
        when(trainerService.getTrainerById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/trainers/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateTrainer_AsAdmin() throws Exception {
        Trainer trainer = new Trainer();
        trainer.setUsername("trainer1");
        when(trainerService.createTrainer(any(Trainer.class))).thenReturn(trainer);
        mockMvc.perform(post("/trainers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trainer)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateTrainer_AsAdmin() throws Exception {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        when(trainerService.updateTrainer(eq(1L), any(Trainer.class))).thenReturn(trainer);
        mockMvc.perform(put("/trainers/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trainer)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteTrainer_AsAdmin() throws Exception {
        doNothing().when(trainerService).deleteTrainer(1L);
        mockMvc.perform(delete("/trainers/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }



}