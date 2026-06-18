package de.hiwiapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@Controller
public class HomeController {

    /**
     * Die zentrale Weiche nach dem Login.
     */
    @GetMapping("/")
    public String index(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        // 1. ADMIN landet STRIKT in der Benutzerverwaltung
        if (roles.contains("ROLE_ADMIN")) {
            return "redirect:/admin/users";
        }

        // 2. BETREUER landet STRIKT auf seinem Dashboard mit den 3 Optionen
        else if (roles.contains("ROLE_BETREUER")) {
            return "redirect:/betreuer/dashboard";
        }

        // 3. STUDENT landet in seinem Bereich
        else if (roles.contains("ROLE_STUDENT")) {
            return "redirect:/student/dashboard";
        }

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String home(Authentication authentication) {
        return index(authentication);
    }
}