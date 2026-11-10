package de.hiwiapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import de.hiwiapp.security.HtmlInputFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. Öffentliche Ressourcen
                        .requestMatchers("/login", "/register", "/error", "/css/**", "/js/**", "/images/**").permitAll()

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
                        .deleteCookies("JSESSIONID")
                        .permitAll()
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