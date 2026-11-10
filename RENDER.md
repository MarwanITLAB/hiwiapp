# HiwiApp auf Render starten (Test / Präsentation)

## A. Datenbank anlegen
1. render.com -> **New** -> **Postgres**
2. Name: `hiwiapp-db`, Region: **Frankfurt (EU Central)**, Plan: **Free** -> **Create Database**
3. Warten bis "Available". Unter **Connections** brauchst du gleich:
   **Hostname** (interner, ohne Punkt, z.B. `dpg-abc123-a`), **Port**, **Database**, **Username**, **Password**

## B. Web Service anlegen
1. **New** -> **Web Service** -> GitHub-Repo `hiwiapp` auswählen
2. Branch: **For-Render**, Region: **Frankfurt** (gleiche wie die Datenbank!)
3. Language / Runtime: **Docker**, Plan: **Free**
4. **Environment Variables** eintragen:

| Key | Value |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://HOSTNAME:5432/DATABASE` (Werte aus A.3 einsetzen) |
| `SPRING_DATASOURCE_USERNAME` | Username aus A.3 |
| `SPRING_DATASOURCE_PASSWORD` | Password aus A.3 |
| `APP_ADMIN_PASSWORD` | ein Passwort für `admin` |
| `APP_DEMO_USERS` | `true` (für die Präsentation: betreuer/student mit 123) |
| `SPRING_THYMELEAF_CACHE` | `true` |
| `TZ` | `Europe/Berlin` |
| `JAVA_TOOL_OPTIONS` | `-XX:MaxRAMPercentage=75` |
| `REMEMBER_ME_KEY` | eine lange, zufällige Zeichenkette (für "Angemeldet bleiben") |

   Achtung: Render zeigt die Adresse als `postgresql://...` – Spring braucht `jdbc:postgresql://...`
   und Benutzer/Passwort NICHT in der URL, sondern in den eigenen Feldern.

5. Unter **Advanced** -> **Health Check Path**: `/login`
6. **Create Web Service** -> Build dauert ca. 5–10 Minuten (Log beobachten)

## C. Testen
Adresse oben im Dashboard, z.B. `https://hiwiapp-xxxx.onrender.com`
- Login `student` / `123`, `betreuer` / `123`, `admin` / dein APP_ADMIN_PASSWORD

## Wichtig für die Präsentation
- Free-Instanz schläft nach 15 Min. ohne Aufruf -> **5 Minuten vorher einmal öffnen**
- E-Mail-Versand geht auf Render Free NICHT (SMTP gesperrt). Beim Anlegen eines Benutzers
  zeigt die App das Passwort stattdessen direkt an.
- Free-Datenbank wird nach 30 Tagen gelöscht – nur Testdaten verwenden.
