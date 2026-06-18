package de.hiwiapp.dto;

public class MonatsEintrag {
    private int monat; // 1 für Januar, 2 für Februar etc.
    private Double stunden;

    private Double sollStunden;

    // Standard-Konstruktor ist für Spring Boot zwingend erforderlich
    public MonatsEintrag() {}

    // Getter und Setter
    public int getMonat() { return monat; }
    public void setMonat(int monat) { this.monat = monat; }
    public Double getStunden() { return stunden; }
    public void setStunden(Double stunden) { this.stunden = stunden; }


    public java.lang.Double getSollStunden() {
        return sollStunden;
    }

    public void setSollStunden(Double sollStunden) {
        this.sollStunden = sollStunden;
    }
}