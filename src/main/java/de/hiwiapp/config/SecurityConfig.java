package de.hiwiapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import de.hiwiapp.security.HtmlInputFilter;
import de.hiwiapp.service.CustomUserDetailsService;

import java.util.UUID;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** "Angemeldet bleiben": so lange bleibt man eingeloggt (30 Tage). */
    private static final int ANGEMELDET_BLEIBEN_SEKUNDEN = 30 * 24 * 60 * 60;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CustomUserDetailsService userDetailsService,
                                           @Value("${app.remember-me-key:}") String rememberMeKey) throws Exception {
        // Geheimer Schlüssel, mit dem das "Angemeldet bleiben"-Cookie signiert wird.
        // Auf dem Server MUSS er per Umgebungsvariable REMEMBER_ME_KEY fest gesetzt werden –
        // sonst wird bei jedem Neustart ein neuer erzeugt und alle müssen sich neu anmelden.
        String schluessel = (rememberMeKey == null || rememberMeKey.isBlank())
                ? UUID.randomUUID().toString() + UUID.randomUUID()
                : rememberMeKey;

        http
                .authorizeHttpRequests(auth -> auth
                        // 1. Öffentliche Ressourcen
                        .requestMatchers("/login", "/register", "/error", "/css/**", "/js/**", "/images/**",
                                "/manifest.json", "/sw.js", "/icons/**").permitAll() // App-Symbol & Installation

                        // 2. Admin & Betreuer dürfen die Benutzerverwaltung nutzen
                        // WICHTIG: Betreuer muss hier rein, damit er speichern darf!
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "BETREUER")

                        // 3. Betreuer-Bereich
                        .requestMatchers("/betreuer/**").hasAnyRole("BETREUER", "ADMIN")

                        // 4. Studenten-Bereich
                        .requestMatchers("/student/**").hasRole("STUDENT")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                )
                // "Angemeldet bleiben" (Haken auf der Login-Seite): man bleibt 30 Tage eingeloggt,
                // auch wenn die App auf dem Handy geschlossen wird. Abmelden löscht das Cookie wieder.
                // Ändert jemand sein Passwort, wird das Cookie automatisch ungültig.
                .rememberMe(rm -> rm
                        .key(schluessel)
                        .rememberMeParameter("remember-me")
                        .rememberMeCookieName("remember-me")
                        .tokenValiditySeconds(ANGEMELDET_BLEIBEN_SEKUNDEN)
                        .userDetailsService(userDetailsService)
                )
                // HTML-Prüfung (AntiSamy) für ALLE Eingaben – auch beim Login.
                // Unerlaubtes HTML -> Log-Eintrag + Nutzer wird ausgeloggt (siehe HtmlInputFilter)
                .addFilterBefore(new HtmlInputFilter(), UsernamePasswordAuthenticationFilter.class)
                // CSRF-Schutz ist AN (Standard). Thymeleaf fügt das nötige Token automatisch
                // in jedes Formular mit th:action ein – deshalb muss jedes POST-Formular th:action nutzen.
                // Abmelden geht deshalb nur noch per POST-Formular (nicht per Link).
                .csrf(csrf -> {});

        return http.build();
    }
}