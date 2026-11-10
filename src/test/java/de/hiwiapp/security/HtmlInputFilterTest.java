package de.hiwiapp.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prüft die AntiSamy-Erkennung ohne Spring und ohne Datenbank.
 * In IntelliJ: Rechtsklick auf die Datei -> "Run 'HtmlInputFilterTest'"
 */
class HtmlInputFilterTest {

    @Test
    void normaleEingabenSindErlaubt() {
        assertTrue(HtmlInputFilter.istHarmlos(null));
        assertTrue(HtmlInputFilter.istHarmlos(""));
        assertTrue(HtmlInputFilter.istHarmlos("Literaturrecherche Kapitel 3"));
        assertTrue(HtmlInputFilter.istHarmlos("Forschung & Lehre"));
        assertTrue(HtmlInputFilter.istHarmlos("4.5"));
        assertTrue(HtmlInputFilter.istHarmlos("Müller-Lüdenscheidt, Straße 5"));
    }

    @Test
    void htmlAngriffeWerdenErkannt() {
        assertFalse(HtmlInputFilter.istHarmlos("<script>alert('x')</script>"));
        assertFalse(HtmlInputFilter.istHarmlos("Hallo<script>alert(1)</script>"));
        assertFalse(HtmlInputFilter.istHarmlos("<img src=x onerror=alert(1)>"));
        assertFalse(HtmlInputFilter.istHarmlos("<iframe src=\"https://boese.example\"></iframe>"));
    }
}
