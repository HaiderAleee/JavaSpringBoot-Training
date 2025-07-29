package com.redmath.SprigSecurity.Google;

import com.redmath.SprigSecurity.Google.User_Table;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User_Table, Long> {
    Optional<User_Table> findByEmail(String email);
}
