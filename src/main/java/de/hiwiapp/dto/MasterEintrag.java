
package de.hiwiapp.dto;

import de.hiwiapp.model.DashboardDaten;
import de.hiwiapp.model.User;

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
}