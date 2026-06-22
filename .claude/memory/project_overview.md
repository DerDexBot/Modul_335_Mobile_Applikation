---
name: project-overview
description: "Schulprojekt Modul 335 – Workforce Management System PlanifyWork, vollständiger Stack-Überblick, was implementiert ist und was noch fehlt"
metadata: 
  node_type: memory
  type: project
  originSessionId: 6896516f-73de-43aa-b3e3-016bb7aaabdd
---

Workforce Management System (PlanifyWork) – Schulprojekt Modul 335.

**Stack:** 3 React-Frontends (Admin :3001, HR :3002, Schichtleiter :3003), Flutter Mobile-App, 8 Spring Boot Microservices hinter einem API-Gateway (:8000), MySQL + MongoDB, alles in Docker Compose.

**Rollen & Demo-Accounts:**
- `admin` / `password` → Admin Web :3001
- `hr.mueller` / `password` → HR Web :3002
- `sl.huber` / `password` → Schichtleiter Web :3003
- `emp.meier` / `password` → Flutter Mobile App

---

## Implementierungsstand pro Service

**Auth Service (:8001):** Login, JWT, Validierung – vollständig.

**User & Role Service (:8002):** Vollständig. GET/POST/PUT /api/users, Seeding der Demo-Accounts beim Start.

**Order Service (:8003):** Vollständig. GET/POST/PUT Aufträge, Zuweisung Schichtleiter/Mitarbeiter, Statusänderung, Download.

**Planning Service (:8004):** Vollständig. HR-Stundenfreigabe (hour-budgets), Arbeitspläne (workplans), Schichten (shifts), Veröffentlichen, Kalender-Endpoint für Mobile App.

**Time Service (:8005):** Vollständig. Check-in/out, Monatsbericht, Pausenverstösse, Gesamtstunden.

**Absence & Vacation Service (:8006):** Vollständig. POST/GET/PUT/DELETE Absenzen, Genehmigung durch HR.

**Billing Service (:8007):** Vollständig. Rechnungen (DRAFT→SENT→PAID), Lohnauszüge (DRAFT→APPROVED→PAID).

**Report/Media Service (:8008):** Vollständig. Bild-Upload in MongoDB, Abruf per ID/Mitarbeiter/Auftrag.

---

## Frontend-Stand

**Admin Web (:3001):**
- Rollen, HR-Benutzer, Mitarbeiter: vollständig API-gestützt
- Aufträge: vollständig über Order Service
- Firmenkonzepte, Stundenregeln, Lohnregeln, Berichte, Audit-Log: noch in localStorage
- Stundenübersicht nicht vom Time Service geladen
- Firmendaten-Seite (/company) fehlt

**HR Web (:3002):** Vollständig (Benutzerverwaltung, Stundenübersicht, Stundenfreigabe, Rechnungen, Lohnauszüge, Absenzen). Fehlt: Abwesenheitskalender-Widget (/absences/calendar).

**Schichtleiter Web (:3003):** Vollständig (Planung, Schichten, Stunden, Aufträge mit Statusänderung). Notizen-Seite (/notes) ist Platzhalter – Schichtnotizen werden aber im Arbeitsplan gespeichert.

**Flutter Mobile App:**
- Check-in/out, Kalender (veröffentlichte Schichten), Absenzen/Ferien, Rapport/Foto-Upload: vollständig
- Fehlt: Auftragsdaten herunterladen (GET /api/orders/{id}/download), Benutzerprofil-Screen

---

## Wichtige technische Details

- Flutter API-URL in `mobile/lib/services/api_config.dart`: `http://10.0.2.2:8000` für Android-Emulator
- MySQL auf Host-Port 3307 (Container 3306), um Konflikt mit lokaler MySQL zu vermeiden
- Planungslogik: approvedHours von HR, plannedHours aus Schichten, overLimit >100%, underPlanned <95%
- Alle API-Calls in Flutter über ApiService, in React über src/services/api.js

**Why:** Schulprojekt Modul 335. HR-Web war die Kernaufgabe, die anderen Teile wurden vom Team ergänzt.

**How to apply:** Stand 2026-06-22 (Ende Session): Das System ist vollständig implementiert und lokal lauffähig. Letzter Git-Commit auf main: `fa88356`. Docker Compose läuft stabil (19 Container). Alle bekannten Bugs in Schichtleiter-Planung und HR-Stundenfreigabe gefixt. Seeding läuft automatisch. Offene Punkte: Flutter Profil-Screen + Auftrags-Download, Admin Stundenübersicht vom Time Service, /company-Seite.

## Seed-Accounts (werden automatisch beim Start angelegt)
- `admin` / `password` → Admin Web :3001 (userId=1)
- `hr.mueller` / `password` → HR Web :3002 (userId=2)
- `sl.huber` / `password` → Schichtleiter Web :3003 (userId=3)
- `emp.meier` / `password` → Flutter Mobile App (userId=4)
- Stundenkontingent für sl.huber (160h, laufender Monat) wird automatisch vom user-role-service geseeded

## Docker Compose Starten
```bash
docker compose down -v   # nur wenn frischer Reset gewünscht
docker compose up --build -d
# Nach ~60s ist API-Gateway bereit
```
