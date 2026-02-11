package de.hiwiapp.hiwiapp.controller;

import de.hiwiapp.hiwiapp.model.Role;
import de.hiwiapp.hiwiapp.model.User;
import de.hiwiapp.hiwiapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // GET: Zeige alle Benutzer
    @GetMapping
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users"; // Template: /templates/admin/users.html
    }

    // POST: Benutzer anlegen
    @PostMapping
    public String createUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam Role role // Spring wandelt automatisch String → Enum
    ) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password)); // Passwort verschlüsseln
        user.setRole(role);

        userRepository.save(user);

        return "redirect:/admin/users"; // Nach Speichern: zeigt aktualisierte Benutzerliste
    }

    // DELETE: Benutzer löschen
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/users";
    }
    // GET: Formular zum Bearbeiten eines Benutzers
    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ungültige Benutzer-ID: " + id));
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values()); // Enum für Dropdown
        return "admin/edit-user"; // Template edit-user.html
    }
    // POST: Speichern der Änderungen
    @PostMapping("/edit/{id}")
    public String updateUser(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam Role role
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ungültige Benutzer-ID: " + id));

        user.setUsername(username);
        user.setEmail(email);
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password)); // Passwort nur ändern, wenn gesetzt
        }
        user.setRole(role);

        userRepository.save(user);
        return "redirect:/admin/users";
    }
}
