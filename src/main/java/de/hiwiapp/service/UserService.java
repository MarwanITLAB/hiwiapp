package de.hiwiapp.service;

import de.hiwiapp.model.User;
import de.hiwiapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Sucht einen Benutzer nach Username.
     * Wir lassen hier Optional, da findByUsername oft für Security-Checks genutzt wird.
     */
    public java.util.Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Liefert alle Benutzer zurück.
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * KORREKTUR: Gibt jetzt direkt einen User zurück.
     * Das löst das Problem im BetreuerController.
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Benutzer mit ID " + id + " nicht gefunden"));
    }

    /**
     * Speichert oder aktualisiert einen Benutzer.
     */
    public User save(User user) {
        return userRepository.save(user);
    }
}