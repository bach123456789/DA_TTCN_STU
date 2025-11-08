package com.web.DA_TTCN_STU.Repositories;

import com.web.DA_TTCN_STU.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
