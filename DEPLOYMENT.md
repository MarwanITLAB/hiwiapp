# HiwiApp auf einem Linux-Server starten

## Voraussetzungen auf dem Server
- Docker + Docker Compose (`docker compose version` muss funktionieren)
- Git

## 1. Projekt holen
```bash
git clone <URL-deines-Repos> hiwiapp
cd hiwiapp
```

## 2. Geheimnisse eintragen
```bash
cp .env.example .env
nano .env
```
Ausfüllen:
- `POSTGRES_PASSWORD` – ein neues, starkes Datenbank-Passwort
- `MAIL_USERNAME` / `MAIL_PASSWORD` – Gmail-Adresse und **App-Passwort**
  (Google-Konto → Sicherheit → 2-Faktor-Authentifizierung → App-Passwörter)
- `APP_ADMIN_PASSWORD` – Passwort für das erste Admin-Konto
- `APP_DEMO_USERS=false` – keine Test-Konten mit Passwort 123
- `REMEMBER_ME_KEY` – lange Zufallszeichenkette für "Angemeldet bleiben" (`openssl rand -hex 32`)

Die `.env` steht in `.gitignore` und darf **nie** nach Git.

## 3. Starten
```bash
docker compose up -d --build
docker compose logs -f app      # Log ansehen (Strg+C zum Beenden)
```
Die App läuft dann auf Port **8080**. Login: `admin` + dein `APP_ADMIN_PASSWORD`.

## 4. Update einspielen
```bash
git pull
docker compose up -d --build
```
Die Datenbank bleibt dabei erhalten (Docker-Volume `postgres_data`).

## Hinweise
- Für den echten Betrieb gehört ein Reverse-Proxy mit HTTPS davor (z.B. nginx oder Caddy),
  damit Passwörter verschlüsselt übertragen werden.
- E-Mail testen: Als Admin einen Benutzer mit deiner eigenen E-Mail-Adresse anlegen.
  Klappt der Versand nicht, steht der Grund im Log (`docker compose logs app`).
