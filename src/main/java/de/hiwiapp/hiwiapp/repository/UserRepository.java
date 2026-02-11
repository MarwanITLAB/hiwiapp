package de.hiwiapp.hiwiapp.repository;

import de.hiwiapp.hiwiapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Diese Methode brauchen wir später für den Login:
    Optional<User> findByUsername(String username);
}