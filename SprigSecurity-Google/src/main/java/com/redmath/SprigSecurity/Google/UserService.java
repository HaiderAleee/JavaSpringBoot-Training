package com.redmath.SprigSecurity.Google;

import com.redmath.SprigSecurity.Google.User_Table;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User_Table createUser(User_Table user) {
        return userRepository.save(user);
    }

    public List<User_Table> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User_Table> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User_Table> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
