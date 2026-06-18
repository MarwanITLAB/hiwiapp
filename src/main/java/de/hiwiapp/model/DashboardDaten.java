package de.hiwiapp.model;

import java.util.List;

public class DashboardDaten {
    private List<MonatsUebersichtDTO> monate;
    private double gesamtSoll;
    private double gesamtIstMitFaktor; // Das ist die Summe der 1,08 Spalte

    public DashboardDaten(List<MonatsUebersichtDTO> monate, double gesamtSoll, double gesamtIstMitFaktor) {
        this.monate = monate;
        this.gesamtSoll = gesamtSoll;
        this.gesamtIstMitFaktor = gesamtIstMitFaktor;
    }

    public List<MonatsUebersichtDTO> getMonate() { return monate; }
    public double getGesamtSoll() { return gesamtSoll; }
    public double getGesamtIstMitFaktor() { return gesamtIstMitFaktor; }
}