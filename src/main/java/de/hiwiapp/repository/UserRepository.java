package de.hiwiapp.repository;

import de.hiwiapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Diese Methode ist wichtig, da du sie in deiner
    // HiwiappApplication (findByUsername) aufrufst!
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);







}