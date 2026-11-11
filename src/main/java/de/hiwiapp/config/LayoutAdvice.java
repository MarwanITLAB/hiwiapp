package de.hiwiapp.config;

import de.hiwiapp.model.User;
import de.hiwiapp.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Liefert für JEDE Seite automatisch die Werte, die die gemeinsame Kopfleiste braucht
 * (templates/fragments/layout.html):
 *  - ${rolle}       "student", "betreuer" oder "admin" -> bestimmt das Menü
 *  - ${anzeigeName} "Vorname Nachname" (oder der Benutzername)
 */
@ControllerAdvice
public class LayoutAdvice {

    private final UserRepository userRepository;

    public LayoutAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute("rolle")
    public String rolle(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        for (GrantedAuthority a : auth.getAuthorities()) {
            switch (a.getAuthority()) {
                case "ROLE_ADMIN": return "admin";
                case "ROLE_BETREUER": return "betreuer";
                case "ROLE_STUDENT": return "student";
                default: break;
            }
        }
        return null;
    }

    @ModelAttribute("anzeigeName")
    public String anzeigeName(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByUsername(auth.getName())
                .map(LayoutAdvice::name)
                .orElse(auth.getName());
    }

    private static String name(User u) {
        String voll = ((u.getVorname() != null ? u.getVorname() : "") + " "
                + (u.getNachname() != null ? u.getNachname() : "")).trim();
        return voll.isEmpty() ? u.getUsername() : voll;
    }
}
