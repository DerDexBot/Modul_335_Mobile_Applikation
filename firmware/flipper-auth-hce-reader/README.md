# Flipper Auth HCE Reader

Custom App für Flipper Zero, die Android HCE über ISO14443-4A liest.

Die App sendet diese SELECT APDU:

```text
00 A4 04 00 0C F0 46 4C 49 50 50 45 52 41 55 54 48 00
```

Erwartete HCE-AID:

```text
F0464C495050455241555448
```

Erwartete Payload:

```text
AUTH|sessionId=...|username=...|action=...|challenge=...
```

Aktuell macht die App:

- ISO14443-4A Polling
- AID per APDU SELECT auswählen
- Antwort mit Status `9000` auswerten
- `sessionId`, `username` und `action` auf dem Display anzeigen
- bei gültiger `sessionId` eine UART-Zeile an das WiFi Devboard senden:

```text
SESSION_ID:<id>
```

Das ist nur ein MVP-Schritt. Die `challenge` wird noch nicht ans Backend gesendet und der Flipper erzeugt noch keine Signatur.

Nicht enthalten:

- Challenge-Weitergabe
- HMAC oder Device-Verifikation

## Build

Mit installiertem `ufbt`:

```powershell
cd firmware/flipper-auth-hce-reader
ufbt
```

Alternativ den Ordner in `applications_user/flipper-auth-hce-reader` einer Flipper-Firmware-Kopie legen und dort bauen.
