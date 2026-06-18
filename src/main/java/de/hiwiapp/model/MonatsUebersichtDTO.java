package de.hiwiapp.model;

public class MonatsUebersichtDTO {
    private String monatName;
    private double summeIst;       // "ist Std.Ztl"
    private double istMitFaktor;   // "ist x 1,08"
    private double summeSoll;      // "soll"
    private double uebertrag;      // "Übertr." (kumuliert)

    public MonatsUebersichtDTO(String monatName, double summeIst, double istMitFaktor, double summeSoll, double uebertrag) {
        this.monatName = monatName;
        this.summeIst = summeIst;
        this.istMitFaktor = istMitFaktor;
        this.summeSoll = summeSoll;
        this.uebertrag = uebertrag;
    }

    // Getter
    public String getMonatName() { return monatName; }
    public double getSummeIst() { return summeIst; }
    public double getIstMitFaktor() { return istMitFaktor; }
    public double getSummeSoll() { return summeSoll; }
    public double getUebertrag() { return uebertrag; }

    // Hilfsmethode für die Anzeige im HTML (formatiert auf 2 Nachkommastellen)
    public String getUebertragFormatiert() {
        return String.format("%.2f", uebertrag);
    }
}