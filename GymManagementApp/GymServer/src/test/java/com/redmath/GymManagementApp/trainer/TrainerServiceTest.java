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
        when(trainerRepo.findById(9L)).thenReturn(Optional.empty());

        Optional<Trainer> result = trainerService.getTrainerById(9L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateTrainer() {
        Trainer trainer = new Trainer();
        trainer.setPassword("raw");

        when(passwordEncoder.encode("raw")).thenReturn("encoded");
        when(trainerRepo.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer result = trainerService.createTrainer(trainer);

        assertEquals("TRAINER", result.getRole());
        assertEquals("encoded", result.getPassword());
        verify(passwordEncoder).encode("raw");
        verify(trainerRepo).save(trainer);
    }

    @Test
    void testUpdateTrainer_WithPasswordProvided() {
        Trainer updated = new Trainer();
        updated.setPassword("newpass");

        when(passwordEncoder.encode("newpass")).thenReturn("encodedNew");
        when(trainerRepo.findById(1L)).thenReturn(Optional.of(new Trainer()));
        when(trainerRepo.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer result = trainerService.updateTrainer(1L, updated);

        assertEquals(1L, result.getId());
        assertEquals("TRAINER", result.getRole());
        assertEquals("encodedNew", result.getPassword());
        verify(passwordEncoder).encode("newpass");
        verify(trainerRepo).save(updated);
    }

    @Test
    void testUpdateTrainer_WithoutPassword_UsesExistingHash() {
        Trainer updated = new Trainer(); // password null/empty triggers else branch
        Trainer existing = new Trainer();
        existing.setPassword("keptHash");

        when(trainerRepo.findById(2L)).thenReturn(Optional.of(existing));
        when(trainerRepo.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer result = trainerService.updateTrainer(2L, updated);

        assertEquals(2L, result.getId());
        assertEquals("TRAINER", result.getRole());
        assertEquals("keptHash", result.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(trainerRepo).save(updated);
    }

    @Test
    void testUpdateTrainer_NotFound_WhenPasswordMissing() {
        Trainer updated = new Trainer(); // no password, so it will look up existing
        when(trainerRepo.findById(3L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> trainerService.updateTrainer(3L, updated));

        assertEquals("Trainer not found", ex.getMessage());
        verify(trainerRepo, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testDeleteTrainer() {
        doNothing().when(trainerRepo).deleteById(1L);
        trainerService.deleteTrainer(1L);
        verify(trainerRepo, times(1)).deleteById(1L);
    }
}
