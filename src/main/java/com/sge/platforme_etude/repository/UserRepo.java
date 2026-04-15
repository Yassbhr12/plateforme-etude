package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.dto.UserDto;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User , Long> {
    Optional<User> findUserByEmail(String email);

    List<User> findUserByNom(String nom);

    List<User> findUserByRole(Role role);

    UserDto findUserById(Long id);
}
