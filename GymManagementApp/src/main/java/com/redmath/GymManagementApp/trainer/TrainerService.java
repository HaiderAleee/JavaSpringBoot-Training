package com.redmath.GymManagementApp.trainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerService {

    @Autowired
    private TrainerRepository trainerRepo;

    public List<Trainer> getAllTrainers() {
        return trainerRepo.findAll();
    }

    public Optional<Trainer> getTrainerById(Long id) {
        return trainerRepo.findById(id);
    }

    public Trainer createTrainer(Trainer trainer) {
        return trainerRepo.save(trainer);
    }

    public Trainer updateTrainer(Long id, Trainer updatedTrainer) {
        updatedTrainer.setId(id);
        return trainerRepo.save(updatedTrainer);
    }

    public void deleteTrainer(Long id) {
        trainerRepo.deleteById(id);
    }
}
