package de.hiwiapp.service;

import de.hiwiapp.dto.MonatsEintrag;
import de.hiwiapp.model.DashboardDaten;
import de.hiwiapp.model.MonatsUebersichtDTO;
import de.hiwiapp.model.User;
import de.hiwiapp.model.WorkLog;
import de.hiwiapp.repository.UserRepository;
import de.hiwiapp.repository.WorkLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class WorkLogService {

    private final WorkLogRepository workLogRepository;
    private final UserRepository userRepository;

    public WorkLogService(WorkLogRepository workLogRepository, UserRepository userRepository) {
        this.workLogRepository = workLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * ZENTRALE AUTOMATIK: Berechnet das Dashboard basierend auf Tages-Logs.
     */
    public DashboardDaten getDashboardDaten(User student) {
        List<MonatsUebersichtDTO> monate = getUebersichtFuerStudent(student);

        double gesamtSoll = monate.stream().mapToDouble(MonatsUebersichtDTO::getSummeSoll).sum();
        double gesamtIstMitFaktor = monate.stream().mapToDouble(MonatsUebersichtDTO::getIstMitFaktor).sum();

        return new DashboardDaten(monate, gesamtSoll, gesamtIstMitFaktor);
    }

    /**
     * BERECHNUNGS-LOGIK: Summiert alle WorkLogs pro Monat automatisch auf.
     * Holt nun die Soll-Stunden spezifisch pro Monat, falls vorhanden.
     */
    public List<MonatsUebersichtDTO> getUebersichtFuerStudent(User student) {
        List<MonatsUebersichtDTO> uebersicht = new ArrayList<>();
        double laufenderUebertrag = 0.0;

        List<WorkLog> allLogs = workLogRepository.findByUser(student);
        int aktuellesJahr = LocalDate.now().getYear();

        for (int i = 1; i <= 12; i++) {
            String monatName = Month.of(i).getDisplayName(TextStyle.FULL, Locale.GERMAN) + " " + aktuellesJahr;
            final int m = i;

            // Ist-Stunden berechnen (Einträge für die monatliche Sollvorgabe werden ignoriert)
            double summeIst = allLogs.stream()
                    .filter(log -> log.getDatum().getMonthValue() == m && log.getDatum().getYear() == aktuellesJahr)
                    .filter(log -> !"Monatliche Sollvorgabe".equals(log.getBeschreibung()))
                    .mapToDouble(WorkLog::getIstStunden)
                    .sum();

            // Soll-Stunden spezifisch für diesen Monat aus den Logs suchen
            double monatlichesSoll = allLogs.stream()
                    .filter(log -> log.getDatum().getMonthValue() == m && log.getDatum().getYear() == aktuellesJahr)
                    .filter(log -> "Monatliche Sollvorgabe".equals(log.getBeschreibung()))
                    .mapToDouble(WorkLog::getSollStunden)
                    .findFirst()
                    .orElse(student.getSollStunden() != null ? student.getSollStunden() : 0.0); // Fallback auf globales User-Soll

            double istMitFaktor = summeIst * 1.08;
            laufenderUebertrag += (istMitFaktor - monatlichesSoll);

            uebersicht.add(new MonatsUebersichtDTO(
                    monatName,
                    summeIst,
                    istMitFaktor,
                    monatlichesSoll,
                    laufenderUebertrag
            ));
        }
        return uebersicht;
    }

    /**
     * KORREKTUR-MODUS: Erlaubt dem Betreuer, die Summen manuell zu überschreiben,
     * falls der Student mal etwas vergessen hat.
     */
    public void aktualisiereMonatsSummen(User student, List<MonatsEintrag> eintraege) {
        for (MonatsEintrag eintrag : eintraege) {
            LocalDate datum = LocalDate.now().withMonth(eintrag.getMonat()).withDayOfMonth(1);

            // Wir suchen nach einem "Sammel-Eintrag" für diesen Monat
            List<WorkLog> vorhandeneLogs = workLogRepository.findByUser(student).stream()
                    .filter(log -> log.getDatum().getMonthValue() == eintrag.getMonat()
                            && log.getDatum().getYear() == datum.getYear()
                            && "Monatliche Gesamteingabe".equals(log.getBeschreibung()))
                    .toList();

            if (!vorhandeneLogs.isEmpty()) {
                WorkLog log = vorhandeneLogs.get(0);
                log.setIstStunden(eintrag.getStunden() != null ? eintrag.getStunden() : 0.0);
                workLogRepository.save(log);
            } else if (eintrag.getStunden() != null && eintrag.getStunden() > 0) {
                // Nur anlegen, wenn wirklich Stunden eingetragen wurden
                WorkLog neuerLog = new WorkLog();
                neuerLog.setUser(student);
                neuerLog.setDatum(datum);
                neuerLog.setIstStunden(eintrag.getStunden());
                neuerLog.setBeschreibung("Monatliche Gesamteingabe");
                workLogRepository.save(neuerLog);
            }
        }
    }

    /**
     * Speichert die monatlich unabhängigen Soll-Stunden als separaten Log-Eintrag.
     * Ersetzt die alte Logik, die nur den Januar global gespeichert hat.
     */
    public void aktualisiereSollVorgaben(User student, List<MonatsEintrag> eintraege) {
        if (eintraege == null) return;

        for (MonatsEintrag eintrag : eintraege) {
            LocalDate datum = LocalDate.now().withMonth(eintrag.getMonat()).withDayOfMonth(1);

            // Wir suchen nach einer bereits existierenden Sollvorgabe für diesen Monat
            List<WorkLog> vorhandeneSollLogs = workLogRepository.findByUser(student).stream()
                    .filter(log -> log.getDatum().getMonthValue() == eintrag.getMonat()
                            && log.getDatum().getYear() == datum.getYear()
                            && "Monatliche Sollvorgabe".equals(log.getBeschreibung()))
                    .toList();

            if (!vorhandeneSollLogs.isEmpty()) {
                // Update des bestehenden Soll-Eintrags für diesen Monat
                WorkLog log = vorhandeneSollLogs.get(0);
                log.setSollStunden(eintrag.getSollStunden() != null ? eintrag.getSollStunden() : 0.0);
                workLogRepository.save(log);
            } else if (eintrag.getSollStunden() != null) {
                // Neuer Soll-Eintrag für diesen Monat, falls noch keiner da war
                WorkLog neuerSollLog = new WorkLog();
                neuerSollLog.setUser(student);
                neuerSollLog.setDatum(datum);
                neuerSollLog.setSollStunden(eintrag.getSollStunden());
                neuerSollLog.setIstStunden(0.0); // Bleibt 0, um die Ist-Stunden nicht zu verfälschen
                neuerSollLog.setBeschreibung("Monatliche Sollvorgabe");
                workLogRepository.save(neuerSollLog);
            }
        }
    }

    public List<WorkLog> findByUser(User user) { return workLogRepository.findByUser(user); }
    public void speichern(WorkLog workLog) { workLogRepository.save(workLog); }
    public void deleteById(Long id) { workLogRepository.deleteById(id); }
    public WorkLog findById(Long id) { return workLogRepository.findById(id).orElseThrow(); }
}