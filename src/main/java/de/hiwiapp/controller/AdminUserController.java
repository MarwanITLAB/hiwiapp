package de.hiwiapp.controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import de.hiwiapp.model.Role;
import de.hiwiapp.model.User;
import de.hiwiapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;



import java.security.SecureRandom;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender; // <-- Aktiviert den Mail-Dienst

    // Absender = das Mail-Konto aus der Konfiguration (Umgebungsvariable MAIL_FROM bzw. MAIL_USERNAME)
    @Value("${app.mail.from}")
    private String mailFrom;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PW_ZEICHEN = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    // Regulärer Ausdruck: Mindestens 8 Zeichen, mindestens 1 Großbuchstabe, mindestens 1 Zahl (für manuelle Änderungen)
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z]).{8,}$";

    // Der Konstruktor initialisiert nun auch den JavaMailSender
    public AdminUserController(UserRepository userRepository, PasswordEncoder passwordEncoder, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    /**
     * Hauptansicht: Zeigt die kombinierte Liste (Kontaktliste + Verwaltung)
     * Pfad im Browser: /admin/users
     * Datei: src/main/resources/templates/admin/users.html
     */
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    /**
     * Erstellt einen neuen Benutzer über das Modal und sendet ein Zufallspasswort per Gmail.
     */
    @PostMapping("/users")
    public String saveNewUser(@ModelAttribute User user, RedirectAttributes redirectAttributes, Model model) {

        // 1. Validierung: Existiert der Benutzername bereits?
        if (userRepository.existsByUsername(user.getUsername())) {
            model.addAttribute("errorMessage", "Der Benutzername '" + user.getUsername() + "' ist bereits vergeben!");
            model.addAttribute("roles", Role.values());
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("user", user); // Behält die eingegebenen Formulardaten bei
            return "admin/users";
        }

        // 2. AUTOMATISCHES ZUFALLSPASSWORT GENERIEREN
        // Erzeugt einen eindeutigen String und schneidet die ersten 10 Zeichen ab (z.B. d3b7f2a1c9)
        String generiertesPasswort = generierePasswort(12);

        // 3. Passwort verschlüsseln für die Datenbank
        user.setPassword(passwordEncoder.encode(generiertesPasswort));

        // Standardwert-Sicherheit: Wenn das Feld leer gelassen wurde, wird es IMMER eine 0.0
        if (user.getSollStunden() == null) {
            user.setSollStunden(0.0);
        }

        // In der Datenbank speichern
        userRepository.save(user);

        // 4. AUTOMATISCHER E-MAIL VERSAND AN DEN NUTZER
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            // WICHTIG: Trage hier in den Anführungszeichen deine neue Gmail-Adresse ein!
            if (mailFrom != null && !mailFrom.isBlank()) {
                message.setFrom(mailFrom);
            }

            message.setTo(user.getEmail()); // Sendet an die im Formular eingetragene E-Mail des Users
            message.setSubject("Dein Zugang zur HiwiApp");
            message.setText("Hallo " + user.getVorname() + " " + user.getNachname() + ",\n\n" +
                    "für dich wurde erfolgreich ein Account in der HiwiApp angelegt.\n\n" +
                    "Deine Zugangsdaten für den ersten Login:\n" +
                    "Benutzername: " + user.getUsername() + "\n" +
                    "Passwort: " + generiertesPasswort + "\n\n" +
                    "Bitte ändere dein Passwort nach dem ersten Login direkt in deinem Profilbereich.\n\n" +
                    "Viele Grüße,\n" +
                    "Dein Systemadministrator");

            mailSender.send(message);
            redirectAttributes.addFlashAttribute("successMessage", "Benutzer erfolgreich angelegt! Die Zugangsdaten wurden per E-Mail versendet.");

        } catch (Exception e) {
            // Grund ins Server-Log schreiben, damit man Mail-Probleme nachvollziehen kann
            System.err.println("Mailversand fehlgeschlagen an " + user.getEmail() + ": " + e.getMessage());
            // Falls Google die Verbindung blockiert oder die Adresse falsch getippt ist, fangen wir das hier ab,
            // damit das Programm nicht abstürzt. Das Passwort wird dann als Warnung ausgegeben.
            redirectAttributes.addFlashAttribute("errorMessage", "Benutzer wurde angelegt, aber die E-Mail konnte nicht gesendet werden! Temporäres Passwort lautet: " + generiertesPasswort);
        }

        return "redirect:/admin/users?success";
    }

    /**
     * Speichert Änderungen aus dem Bearbeiten-Modal mit Passwortprüfung.
     */
    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User formDaten, RedirectAttributes redirectAttributes, Model model) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden: " + id));

        // Validierung: Wenn ein neues Passwort eingetippt wurde, muss es stark sein
        if (formDaten.getPassword() != null && !formDaten.getPassword().isEmpty()) {
            if (!formDaten.getPassword().matches(PASSWORD_PATTERN)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Änderung fehlgeschlagen: Das neue Passwort für '" + existingUser.getUsername() + "' ist zu schwach! Mindestens 8 Zeichen, ein Großbuchstabe und eine Zahl erforderlich.");
                return "redirect:/admin/users?error";
            }
            existingUser.setPassword(passwordEncoder.encode(formDaten.getPassword()));
        }

        // Account-Daten aktualisieren
        existingUser.setUsername(formDaten.getUsername());
        existingUser.setEmail(formDaten.getEmail());

        if (formDaten.getRole() != null) {
            existingUser.setRole(formDaten.getRole());
        }

        // Auch beim Bearbeiten: Wenn das Feld geleert wurde, setzen wir es auf 0.0
        if (formDaten.getSollStunden() == null) {
            existingUser.setSollStunden(0.0);
        } else {
            existingUser.setSollStunden(formDaten.getSollStunden());
        }

        // Persönliche Profildaten für die Kontaktliste
        existingUser.setVorname(formDaten.getVorname());
        existingUser.setNachname(formDaten.getNachname());
        existingUser.setTelefon(formDaten.getTelefon());
        existingUser.setAnschrift(formDaten.getAnschrift());
        existingUser.setGeburtsdatum(formDaten.getGeburtsdatum());

        userRepository.save(existingUser);
        redirectAttributes.addFlashAttribute("successMessage", "Benutzer '" + existingUser.getUsername() + "' erfolgreich aktualisiert!");
        return "redirect:/admin/users?updated";
    }

    /**
     * Löscht einen Benutzer.
     */
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Benutzer erfolgreich gelöscht!");
        return "redirect:/admin/users?deleted";
    }

    /** Erzeugt ein zufälliges Passwort (kryptografisch sicher, ohne verwechselbare Zeichen wie 0/O, 1/l). */
    private static String generierePasswort(int laenge) {
        StringBuilder sb = new StringBuilder(laenge);
        for (int i = 0; i < laenge; i++) {
            sb.append(PW_ZEICHEN.charAt(RANDOM.nextInt(PW_ZEICHEN.length())));
        }
        return sb.toString();
    }
}
