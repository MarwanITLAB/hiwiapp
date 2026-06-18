package de.hiwiapp.controller;

import de.hiwiapp.dto.StundenUpdateForm;
import de.hiwiapp.dto.MonatsEintrag;
import de.hiwiapp.model.DashboardDaten;
import de.hiwiapp.model.User;
import de.hiwiapp.model.WorkLog;
import de.hiwiapp.service.UserService;
import de.hiwiapp.service.WorkLogService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final WorkLogService workLogService;
    private final UserService userService;
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.GERMAN);

    public StudentController(WorkLogService workLogService, UserService userService) {
        this.workLogService = workLogService;
        this.userService = userService;
    }

    /**
     * Dashboard: Zeigt Statistiken und das Quick-Add Formular an.
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User student = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        DashboardDaten dashboardDaten = workLogService.getDashboardDaten(student);

        model.addAttribute("user", student);
        model.addAttribute("daten", dashboardDaten);

        // Vorbereitung für das Quick-Add Formular (Heute-Eintrag)
        WorkLog heuteLog = new WorkLog();
        heuteLog.setDatum(LocalDate.now());
        model.addAttribute("workLog", heuteLog);

        return "student/dashboard";
    }

    /**
     * Speichert einen neuen WorkLog-Eintrag direkt vom Dashboard.
     */
    @PostMapping("/add")
    public String addWorkLog(@ModelAttribute("workLog") WorkLog workLog,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User student = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        workLog.setUser(student);
        workLogService.speichern(workLog);

        // Rückkehr zum Dashboard mit Erfolgsmeldung
        return "redirect:/student/dashboard?success";
    }

    /**
     * Monatsliste: Zeigt alle einzelnen Einträge eines Monats an.
     */
    @GetMapping("/monat/{monatName}")
    public String showMonatDetails(@PathVariable String monatName,
                                   Model model,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        User student = userService.findByUsername(userDetails.getUsername()).orElseThrow();

        List<WorkLog> logsInMonat = workLogService.findByUser(student).stream()
                .filter(log -> log.getDatum().format(monthFormatter).equalsIgnoreCase(monatName))
                .toList();

        double netto = logsInMonat.stream()
                .mapToDouble(l -> l.getIstStunden() != null ? l.getIstStunden() : 0.0)
                .sum();

        model.addAttribute("user", student);
        model.addAttribute("ausgewaehlterMonat", monatName);
        model.addAttribute("logsInMonat", logsInMonat);
        model.addAttribute("totalNetto", netto);
        model.addAttribute("totalBrutto", netto * 1.08); // Brutto-Logik nur in der Übersicht

        return "student/monat-detail";
    }

    /**
     * Jahresübersicht: Ermöglicht das Bearbeiten der Soll/Ist-Summen pro Monat.
     */
    @GetMapping("/bearbeiten")
    public String showBearbeitenAnsicht(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User student = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        DashboardDaten dashboardDaten = workLogService.getDashboardDaten(student);

        model.addAttribute("user", student);
        model.addAttribute("daten", dashboardDaten);

        StundenUpdateForm form = new StundenUpdateForm();
        for (int i = 0; i < dashboardDaten.getMonate().size(); i++) {
            var m = dashboardDaten.getMonate().get(i);
            MonatsEintrag e = new MonatsEintrag();
            e.setMonat(i + 1);
            e.setStunden(m.getSummeIst());
            e.setSollStunden(m.getSummeSoll());
            form.getEintraege().add(e);
        }

        model.addAttribute("stundenForm", form);
        model.addAttribute("isBetreuer", false);
        model.addAttribute("studentId", student.getId());
        return "student/stunden-bearbeiten";
    }

    /**
     * Speichert die Korrekturen aus der Jahresübersicht.
     */
    @PostMapping("/speichern")
    public String speichereAlleStunden(@ModelAttribute("stundenForm") StundenUpdateForm form,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        User student = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        workLogService.aktualisiereMonatsSummen(student, form.getEintraege());
        return "redirect:/student/dashboard?success";
    }

    @GetMapping("/aktueller-monat")
    public String redirectToCurrentMonth() {
        return "redirect:/student/monat/" + LocalDate.now().format(monthFormatter);
    }

    @PostMapping("/delete/{id}")
    public String deleteWorkLog(@PathVariable Long id) {
        WorkLog log = workLogService.findById(id);
        String monatName = log.getDatum().format(monthFormatter);
        workLogService.deleteById(id);
        return "redirect:/student/monat/" + monatName;
    }

    @GetMapping("/profil")
    public String showProfil(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User student = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("user", student);
        return "student/profil";
    }
}