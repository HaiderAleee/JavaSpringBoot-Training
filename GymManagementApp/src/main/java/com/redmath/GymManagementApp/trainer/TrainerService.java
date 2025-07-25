package com.redmath.GymManagementApp.trainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerService {

    @Autowired
    private TrainerRepository trainerRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Trainer> getAllTrainers() {
        return trainerRepo.findAll();
    }

    public Optional<Trainer> getTrainerById(Long id) {
        return trainerRepo.findById(id);
    }

    public Trainer createTrainer(Trainer trainer) {
        trainer.setRole("TRAINER");
        trainer.setPassword(passwordEncoder.encode(trainer.getPassword()));
        return trainerRepo.save(trainer);
    }

    public Trainer updateTrainer(Long id, Trainer updatedTrainer) {
        updatedTrainer.setId(id);
        updatedTrainer.setRole("TRAINER");

        if (updatedTrainer.getPassword() != null && !updatedTrainer.getPassword().isEmpty()) {
            updatedTrainer.setPassword(passwordEncoder.encode(updatedTrainer.getPassword()));
        } else {
            Trainer existingTrainer = trainerRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Trainer not found"));
            updatedTrainer.setPassword(existingTrainer.getPassword());
        }

        return trainerRepo.save(updatedTrainer);
    }

    public void deleteTrainer(Long id) {
        trainerRepo.deleteById(id);
    }
}
