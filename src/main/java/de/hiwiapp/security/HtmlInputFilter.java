package de.hiwiapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Prüft JEDE Eingabe (alle Formularfelder und URL-Parameter) mit AntiSamy.
 *
 * Enthält ein Feld unerlaubtes HTML (z.B. &lt;script&gt;, onerror=..., iframe):
 *   1. Eintrag ins Sicherheits-Log (Logger "SECURITY") mit Benutzer, IP, Adresse und Feldname
 *   2. Der Benutzer wird sofort ausgeloggt
 *   3. Weiterleitung auf /login?blocked – die Eingabe wird NICHT gespeichert
 *
 * Wird in SecurityConfig eingehängt (bewusst KEINE @Component, sonst liefe der Filter doppelt).
 */
public class HtmlInputFilter extends OncePerRequestFilter {

    private static final Logger SECURITY_LOG = LoggerFactory.getLogger("SECURITY");

    /**
     * Felder, die nicht geprüft werden:
     *  - Passwörter werden nie angezeigt, nur verschlüsselt gespeichert. Ein starkes Passwort darf
     *    deshalb auch Zeichen wie &lt; oder &gt; enthalten, ohne dass der Nutzer rausfliegt.
     *  - _csrf ist das interne Sicherheits-Token von Spring.
     */
    private static final Set<String> NICHT_PRUEFEN = Set.of("password", "_csrf");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        for (Map.Entry<String, String[]> feld : request.getParameterMap().entrySet()) {
            if (NICHT_PRUEFEN.contains(feld.getKey())) continue;

            for (String wert : feld.getValue()) {
                if (!istHarmlos(wert)) {
                    blockieren(request, response, feld.getKey(), wert);
                    return; // Anfrage NICHT weiterreichen -> nichts wird gespeichert
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Harmlos = AntiSamy entfernt nichts.
     * Normale Sonderzeichen wie "&", "<" oder ">" in einem Text ("Forschung & Lehre", "a < b")
     * wandelt AntiSamy nur in &amp;amp; usw. um. Das wird hier zurückgewandelt, damit solche
     * Texte NICHT als Angriff gelten. Entfernt AntiSamy dagegen Tags oder Attribute, ist die Eingabe unerlaubt.
     */
    static boolean istHarmlos(String eingabe) {
        if (eingabe == null || eingabe.isBlank()) return true;
        // Schneller Weg: Ohne "<" kann kein HTML-Tag enthalten sein
        if (eingabe.indexOf('<') < 0) return true;
        try {
            String bereinigt = Util.toSafeHtml(eingabe);
            return normalisiere(HtmlUtils.htmlUnescape(bereinigt)).equals(normalisiere(eingabe));
        } catch (Exception e) {
            // Kann AntiSamy die Eingabe nicht prüfen, behandeln wir sie sicherheitshalber als unerlaubt
            SECURITY_LOG.warn("AntiSamy-Prüfung fehlgeschlagen: {}", e.getMessage());
            return false;
        }
    }

    /** Leerzeichen/Zeilenumbrüche vereinheitlichen, damit nur echte Änderungen zählen. */
    private static String normalisiere(String s) {
        return s.replaceAll("\\s+", " ").trim();
    }

    private void blockieren(HttpServletRequest request, HttpServletResponse response,
                            String feldName, String wert) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String benutzer = (auth != null && auth.isAuthenticated()) ? auth.getName() : "nicht angemeldet";

        // Log-Eintrag. Zeilenumbrüche werden entfernt (sonst könnte man gefälschte Log-Zeilen einschleusen)
        SECURITY_LOG.warn("Unerlaubtes HTML blockiert und Nutzer ausgeloggt: benutzer={}, ip={}, methode={}, adresse={}, feld={}, eingabe={}",
                fuerLog(benutzer), request.getRemoteAddr(), request.getMethod(),
                fuerLog(request.getRequestURI()), fuerLog(feldName), fuerLog(kuerze(wert, 200)));

        // Ausloggen: Session beenden und Anmeldung löschen
        new SecurityContextLogoutHandler().logout(request, response, auth);

        response.sendRedirect(request.getContextPath() + "/login?blocked");
    }

    private static String fuerLog(String s) {
        return s == null ? "" : s.replaceAll("[\\r\\n\\t]", "_");
    }

    private static String kuerze(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
