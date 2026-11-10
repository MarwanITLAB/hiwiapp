
package de.hiwiapp.dto;

import de.hiwiapp.model.DashboardDaten;
import de.hiwiapp.model.MonatsUebersichtDTO;
import de.hiwiapp.model.User;

import java.time.LocalDate;
import java.util.List;

/**
 * Hilfsklasse für den Stunden-Master, um pro Student
 * direkt die berechneten Daten mitzuliefern.
 */
public class MasterEintrag {
    private User student;
    private DashboardDaten daten;

    public MasterEintrag(User student, DashboardDaten daten) {
        this.student = student;
        this.daten = daten;
    }

    public User getStudent() { return student; }
    public DashboardDaten getDaten() { return daten; }

    /**
     * Kumulierter Übertrag bis EINSCHLIESSLICH des aktuellen Monats.
     * Zukünftige Monate zählen nicht mit – sonst wäre im Laufe des Jahres jeder "im Minus".
     */
    public double getStandHeute() {
        List<MonatsUebersichtDTO> monate = daten.getMonate();
        if (monate == null || monate.isEmpty()) return 0.0;
        int index = Math.min(LocalDate.now().getMonthValue(), monate.size()) - 1;
        return monate.get(index).getUebertrag();
    }

    public boolean isImMinus() { return getStandHeute() < 0; }
}
