// Service Worker der HiwiApp.
// Er macht die Web-App "installierbar" (Symbol auf dem Startbildschirm).
// Bewusst KEIN Zwischenspeichern von Seiten: Stunden und Login sollen immer aktuell vom Server kommen.
self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', (event) => event.waitUntil(self.clients.claim()));
self.addEventListener('fetch', (event) => {
    // Ohne Internet eine verständliche Meldung statt einer Browser-Fehlerseite
    if (event.request.mode === 'navigate') {
        event.respondWith(
            fetch(event.request).catch(() => new Response(
                '<!doctype html><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">' +
                '<body style="font-family:sans-serif;text-align:center;padding:40px">' +
                '<h2>Keine Internetverbindung</h2><p>Bitte prüfe dein Netz und versuche es erneut.</p>' +
                '<button onclick="location.reload()" style="padding:12px 20px;border-radius:12px;border:none;background:#0d6efd;color:#fff">Erneut versuchen</button></body>',
                { headers: { 'Content-Type': 'text/html; charset=utf-8' } }))
        );
    }
});
