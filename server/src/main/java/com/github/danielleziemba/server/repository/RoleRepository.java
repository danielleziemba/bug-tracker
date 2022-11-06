package com.github.danielleziemba.server.repository;

import com.github.danielleziemba.server.models.ERole;
import com.github.danielleziemba.server.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
