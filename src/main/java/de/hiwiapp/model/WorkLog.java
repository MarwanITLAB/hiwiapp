package de.hiwiapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "work_logs")
public class WorkLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate datum;

    private String beschreibung;

    @Column(nullable = false)
    private Double istStunden;

    @Column(name = "soll_stunden")
    private Double sollStunden = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // --- Konstruktoren ---
    public WorkLog() {
    }

    public WorkLog(LocalDate datum, String beschreibung, Double istStunden, User user) {
        this.datum = datum;
        this.beschreibung = beschreibung;
        this.istStunden = istStunden;
        this.user = user;
        this.sollStunden = 0.0;
    }

    // --- Getter und Setter ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public Double getIstStunden() {
        return istStunden;
    }

    public void setIstStunden(Double istStunden) {
        this.istStunden = istStunden;
    }

    public Double getSollStunden() {
        return sollStunden;
    }

    public void setSollStunden(Double sollStunden) {
        this.sollStunden = sollStunden;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "WorkLog{" +
                "id=" + id +
                ", datum=" + datum +
                ", istStunden=" + istStunden +
                ", sollStunden=" + sollStunden +
                '}';
    }
}