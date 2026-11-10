package de.hiwiapp.controller;

import de.hiwiapp.dto.MasterEintrag;
import de.hiwiapp.dto.MonatsEintrag;
import de.hiwiapp.dto.StundenUpdateForm;
import de.hiwiapp.model.DashboardDaten;
import de.hiwiapp.model.Role;
import de.hiwiapp.model.User;
import de.hiwiapp.model.WorkLog;
import de.hiwiapp.service.UserService;
import de.hiwiapp.service.WorkLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/betreuer")
public class BetreuerController {

    private final UserService userService;
    private final WorkLogService workLogService;

    public BetreuerController(UserService userService, WorkLogService workLogService) {
        this.userService = userService;
        this.workLogService = workLogService;
    }

    @GetMapping("/dashboard")
    public String showDashboard() {
        return "betreuer/dashboard";
    }

    /**
     * Die klassische Master-Liste mit der Jahresübersicht aller Hiwis,
     * optional sortierbar nach Minusstunden (über ?sort=minus).
     */
    @GetMapping("/stunden")
    public String showStundenMaster(
            @RequestParam(name = "sort", required = false) String sort,
            Model model) {

        List<User> studenten = userService.findAll().stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .collect(Collectors.toList());

        List<MasterEintrag> masterListe = studenten.stream()
                .map(s -> new MasterEintrag(s, workLogService.getDashboardDaten(s)))
                .collect(Collectors.toList());

        // NEU: Wenn der Sortier-Parameter "minus" aktiv ist, nach dem Übertrag sortieren
        if ("minus".equals(sort)) {
            // Größtes Minus zuerst – Stand bis zum aktuellen Monat (nicht Dezember)
            masterListe.sort(Comparator.comparingDouble(MasterEintrag::getStandHeute));
        }

        model.addAttribute("masterListe", masterListe);
        model.addAttribute("noData", masterListe.isEmpty());
        model.addAttribute("currentSort", sort);

        return "betreuer/stunden-master";
    }

    /**
     * KONTAKTLISTE & VERWALTUNG
     */
    @GetMapping("/kontakte")
    public String showKontaktliste(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    /**
     * ÖFFNEN: Lädt die monatliche Bearbeitungsseite für einen spezifischen Studenten.
     */
    @GetMapping("/edit-student/{id}")
    public String zeigeBearbeitungFuerBetreuer(@PathVariable Long id, Model model) {
        User student = userService.findById(id);
        DashboardDaten daten = workLogService.getDashboardDaten(student);

        StundenUpdateForm form = new StundenUpdateForm();
        for (int i = 0; i < daten.getMonate().size(); i++) {
            var m = daten.getMonate().get(i);
            MonatsEintrag e = new MonatsEintrag();
            e.setMonat(i + 1);
            e.setStunden(m.getSummeIst());
            e.setSollStunden(m.getSummeSoll());
            form.getEintraege().add(e);
        }

        model.addAttribute("daten", daten);
        model.addAttribute("stundenForm", form);
        model.addAttribute("studentId", id);
        model.addAttribute("isBetreuer", true);

        return "student/stunden-bearbeiten";
    }

    /**
     * SPEICHERN: Verarbeitet Korrekturen an Ist- und Soll-Stunden eines Hiwis.
     */
    @PostMapping("/save-student/{id}")
    public String speichereStudentDurchBetreuer(@PathVariable Long id, @ModelAttribute("stundenForm") StundenUpdateForm form) {
        User student = userService.findById(id);
        workLogService.aktualisiereMonatsSummen(student, form.getEintraege());
        workLogService.aktualisiereSollVorgaben(student, form.getEintraege());
        return "redirect:/betreuer/stunden?success";
    }

    /**
     * NEU: Zeigt die Monatsliste (Arbeitstage) eines Hiwis für den Betreuer an.
     * Nutzt das existierende Template student/monat-detail.html
     */
    @GetMapping("/student/{studentId}/monat/{monatName}")
    public String showStudentMonatDetails(@PathVariable Long studentId, @PathVariable String monatName, Model model) {
        User student = userService.findById(studentId);

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.GERMAN);

        List<WorkLog> logsInMonat = workLogService.findByUser(student).stream()
                .filter(log -> log.getDatum().format(monthFormatter).equalsIgnoreCase(monatName))
                .collect(Collectors.toList());

        double netto = logsInMonat.stream()
                .mapToDouble(l -> l.getIstStunden() != null ? l.getIstStunden() : 0.0)
                .sum();

        model.addAttribute("user", student);
        model.addAttribute("ausgewaehlterMonat", monatName);
        model.addAttribute("logsInMonat", logsInMonat);
        model.addAttribute("totalNetto", netto);
        model.addAttribute("isBetreuer", true);
        model.addAttribute("monatGesperrt", false);

        return "student/monat-detail";
    }
}