package de.hiwiapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;

    // --- NEUE KONTAKTDATEN FÜR DAS DASHBOARD ---
    private String vorname;
    private String nachname;
    private String telefon;
    private String anschrift;
    private LocalDate geburtsdatum;

    @Column(length = 1000) // Erlaubt längere Notizen
    private String anmerkung;
    // -------------------------------------------

    private Double sollStunden;

    @Enumerated(EnumType.STRING)
    private Role role;

    public User() {}

    // Vorhandene Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Double getSollStunden() { return sollStunden; }
    public void setSollStunden(Double sollStunden) { this.sollStunden = sollStunden; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    // NEUE Getter/Setter für die Kontaktübersicht
    public String getVorname() { return vorname; }
    public void setVorname(String vorname) { this.vorname = vorname; }

    public String getNachname() { return nachname; }
    public void setNachname(String nachname) { this.nachname = nachname; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public String getAnschrift() { return anschrift; }
    public void setAnschrift(String anschrift) { this.anschrift = anschrift; }

    public LocalDate getGeburtsdatum() { return geburtsdatum; }
    public void setGeburtsdatum(LocalDate geburtsdatum) { this.geburtsdatum = geburtsdatum; }

    public String getAnmerkung() { return anmerkung; }
    public void setAnmerkung(String anmerkung) { this.anmerkung = anmerkung; }
}