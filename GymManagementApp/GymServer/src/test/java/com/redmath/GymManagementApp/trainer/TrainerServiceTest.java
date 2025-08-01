package com.redmath.GymManagementApp.trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllTrainers() {
        when(trainerRepo.findAll()).thenReturn(List.of(new Trainer()));
        List<Trainer> trainers = trainerService.getAllTrainers();
        assertFalse(trainers.isEmpty());
    }

    @Test
    void testGetTrainerById_Found() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        when(trainerRepo.findById(1L)).thenReturn(Optional.of(trainer));
        Optional<Trainer> result = trainerService.getTrainerById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void testGetTrainerById_NotFound() {
        when(trainerRepo.findById(1L)).thenReturn(Optional.empty());
        Optional<Trainer> result = trainerService.getTrainerById(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateTrainer() {
        Trainer trainer = new Trainer();
        trainer.setPassword("raw");
        when(passwordEncoder.encode("raw")).thenReturn("encoded");
        when(trainerRepo.save(any(Trainer.class))).thenReturn(trainer);
        Trainer result = trainerService.createTrainer(trainer);
        assertEquals("TRAINER", result.getRole());
        assertEquals("encoded", result.getPassword());
    }

    @Test
    void testUpdateTrainer() {
        Trainer updated = new Trainer();
        updated.setPassword("newpass");
        Trainer existing = new Trainer();
        existing.setPassword("oldpass");
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");
        when(trainerRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(trainerRepo.save(any(Trainer.class))).thenReturn(updated);
        Trainer result = trainerService.updateTrainer(1L, updated);
        assertEquals("TRAINER", result.getRole());
        assertEquals("encoded", result.getPassword());
    }

    @Test
    void testDeleteTrainer() {
        doNothing().when(trainerRepo).deleteById(1L);
        trainerService.deleteTrainer(1L);
        verify(trainerRepo, times(1)).deleteById(1L);
    }
}