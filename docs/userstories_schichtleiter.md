Unten sind die **Userstories für die Rolle Schichtleiter**. Ich habe mich an eurem Userstory-Template orientiert und die Funktionen aus README/Projektbeschreibung übernommen: Schichtleiter-Web, Planning Service, Order Service, Time Service, Arbeitspläne, Aufträge, Notizen und die Stundenplanung mit HR-Freigabe. fileciteturn3file1 fileciteturn3file0

---

# Userstories – Schichtleiter

## US-SL-001: Schichtleiter-Dashboard anzeigen

### Userstory

Als **Schichtleiter** möchte ich **nach dem Login ein Dashboard mit meinen wichtigsten Informationen sehen**, damit **ich schnell erkenne, welche Arbeitspläne, Aufträge und Stunden relevant sind**.

### Beschreibung

Der Schichtleiter sieht nach dem Login eine Übersicht über aktuelle Arbeitspläne, offene Aufträge, geplante Stunden, verfügbare HR-Stunden und Warnungen bei Über- oder Unterplanung.

### Akzeptanzkriterien

#### AK-1: Dashboard erfolgreich anzeigen

**Given** ein Schichtleiter ist erfolgreich angemeldet  
**When** er das Schichtleiter-Web öffnet  
**Then** sieht er ein Dashboard mit Arbeitsplänen, Aufträgen und Stundenübersicht

#### AK-2: Nur eigene Daten anzeigen

**Given** ein Schichtleiter ist angemeldet  
**When** das Dashboard geladen wird  
**Then** werden nur die für ihn relevanten Teams, Aufträge und Arbeitspläne angezeigt

#### AK-3: Warnungen sichtbar anzeigen

**Given** ein Arbeitsplan überschreitet oder unterschreitet das verfügbare Stundenkontingent  
**When** der Schichtleiter das Dashboard öffnet  
**Then** wird eine verständliche Warnung angezeigt

### Randfälle und Fehlerfälle

- Keine Arbeitspläne vorhanden
- Keine Aufträge zugewiesen
- Stundenkontingent von HR fehlt
- Server oder Datenbank nicht erreichbar
- Benutzer hat nicht die Rolle `SHIFT_LEAD`

### Nicht-funktionale Anforderungen

- Performance: Dashboard soll innerhalb weniger Sekunden laden.
- Sicherheit: Zugriff nur für Schichtleiter.
- Usability: Warnungen müssen gut sichtbar und verständlich sein.

### Abhängigkeiten

- Auth Service
- Planning Service
- Order Service
- Time Service
- HR-Freigabe der verfügbaren Stunden

### Offene Fragen

- Soll das Dashboard Tages-, Wochen- oder Monatsdaten standardmässig anzeigen?
- Welche Warnfarben sollen verwendet werden?

---

## US-SL-002: Arbeitsplan für einen Monat erstellen

### Userstory

Als **Schichtleiter** möchte ich **einen Arbeitsplan für einen Monat erstellen**, damit **ich meine Mitarbeiter passend zu den verfügbaren Arbeitsstunden einteilen kann**.

### Beschreibung

Der Schichtleiter erstellt einen monatlichen Arbeitsplan. Dabei sieht er das von der HR freigegebene Stundenkontingent, zum Beispiel 1000 Stunden für einen Monat.

### Akzeptanzkriterien

#### AK-1: Neuen Arbeitsplan erstellen

**Given** ein Schichtleiter befindet sich auf der Planungsseite  
**When** er einen Monat auswählt und einen neuen Arbeitsplan erstellt  
**Then** wird ein leerer Arbeitsplan für diesen Monat angelegt

#### AK-2: HR-Stundenkontingent anzeigen

**Given** die HR hat ein Stundenkontingent für den Monat freigegeben  
**When** der Schichtleiter den Arbeitsplan öffnet  
**Then** sieht er die maximal verfügbaren Arbeitsstunden für diesen Monat

#### AK-3: Arbeitsplan speichern

**Given** ein Arbeitsplan enthält gültige Daten  
**When** der Schichtleiter den Arbeitsplan speichert  
**Then** wird der Arbeitsplan gespeichert und kann später weiterbearbeitet werden

### Randfälle und Fehlerfälle

- Monat wurde nicht ausgewählt
- HR hat noch kein Stundenkontingent freigegeben
- Arbeitsplan für diesen Monat existiert bereits
- Speichern schlägt wegen Serverfehler fehl

### Nicht-funktionale Anforderungen

- Sicherheit: Nur berechtigte Schichtleiter dürfen Arbeitspläne erstellen.
- Usability: Monatsauswahl muss einfach bedienbar sein.
- Datenintegrität: Arbeitspläne dürfen nicht doppelt für denselben Bereich erstellt werden.

### Abhängigkeiten

- Planning Service
- HR-Stundenfreigabe
- User & Role Service

### Offene Fragen

- Darf ein Schichtleiter mehrere Arbeitspläne pro Monat erstellen?
- Gilt das Stundenkontingent pro Firma, Abteilung, Auftrag oder Schichtleiter?

---

## US-SL-003: Schichten zu einem Arbeitsplan hinzufügen

### Userstory

Als **Schichtleiter** möchte ich **Schichten zu einem Arbeitsplan hinzufügen**, damit **Mitarbeiter wissen, wann sie arbeiten müssen**.

### Beschreibung

Der Schichtleiter kann pro Mitarbeiter Schichten mit Datum, Startzeit, Endzeit und optionalem Auftrag erfassen.

### Akzeptanzkriterien

#### AK-1: Schicht hinzufügen

**Given** ein Arbeitsplan ist geöffnet  
**When** der Schichtleiter Mitarbeiter, Datum, Startzeit und Endzeit eingibt  
**Then** wird die Schicht dem Arbeitsplan hinzugefügt

#### AK-2: Geplante Stunden berechnen

**Given** eine Schicht enthält Start- und Endzeit  
**When** die Schicht gespeichert wird  
**Then** werden die geplanten Stunden automatisch berechnet

#### AK-3: Schicht im Kalender anzeigen

**Given** eine Schicht wurde gespeichert  
**When** der Arbeitsplan angezeigt wird  
**Then** erscheint die Schicht im Kalender des Schichtleiters

### Randfälle und Fehlerfälle

- Startzeit liegt nach Endzeit
- Mitarbeiter wurde nicht ausgewählt
- Schicht überschneidet sich mit einer bestehenden Schicht
- Datum liegt ausserhalb des gewählten Monats
- Mitarbeiter ist abwesend oder in den Ferien

### Nicht-funktionale Anforderungen

- Usability: Schichten sollen einfach über Kalenderansicht erfassbar sein.
- Performance: Änderungen sollen ohne lange Ladezeit sichtbar sein.
- Datenqualität: Ungültige Zeiten dürfen nicht gespeichert werden.

### Abhängigkeiten

- Planning Service
- User & Role Service
- Absence & Vacation Service
- Datenbanktabellen `work_plans` und `shifts`

### Offene Fragen

- Sollen Schichten per Drag-and-drop verschoben werden können?
- Sollen Pausen direkt in der Schicht erfasst werden?

---

## US-SL-004: Geplante Stunden mit HR-Kontingent vergleichen

### Userstory

Als **Schichtleiter** möchte ich **jederzeit sehen, wie viele Stunden bereits geplant sind**, damit **ich das von der HR freigegebene Stundenkontingent einhalten kann**.

### Beschreibung

Während der Planung sieht der Schichtleiter die Summe aller geplanten Stunden und das verfügbare Monatskontingent. Bei 1000 freigegebenen Stunden sieht er zum Beispiel, ob 850, 1000 oder 1050 Stunden geplant sind.

### Akzeptanzkriterien

#### AK-1: Geplante Stunden anzeigen

**Given** ein Arbeitsplan enthält mehrere Schichten  
**When** der Schichtleiter den Plan öffnet  
**Then** sieht er die Summe aller geplanten Stunden

#### AK-2: Reststunden anzeigen

**Given** ein HR-Stundenkontingent ist vorhanden  
**When** Schichten geplant werden  
**Then** zeigt das System an, wie viele Stunden noch verfügbar sind

#### AK-3: Überschreitung anzeigen

**Given** das HR-Kontingent beträgt 1000 Stunden  
**When** der Schichtleiter 1001 oder mehr Stunden plant  
**Then** wird eine Warnung angezeigt

### Randfälle und Fehlerfälle

- HR-Kontingent fehlt
- Stundenberechnung schlägt fehl
- Schichten enthalten ungültige Zeiten
- Rundungsfehler bei Minutenberechnung

### Nicht-funktionale Anforderungen

- Genauigkeit: Stunden müssen korrekt aus Start- und Endzeit berechnet werden.
- Usability: Die Stundenübersicht muss während der Planung sichtbar bleiben.
- Transparenz: Überschreitung und Reststunden müssen klar erkennbar sein.

### Abhängigkeiten

- Planning Service
- Time Service oder interne Stundenberechnung im Planning Service
- HR-Freigabe der Monatsstunden

### Offene Fragen

- Sollen Pausen von den geplanten Stunden abgezogen werden?
- Soll bei Überschreitung nur gewarnt oder das Speichern verhindert werden?

---

## US-SL-005: Warnung bei zu vielen oder zu wenigen geplanten Stunden erhalten

### Userstory

Als **Schichtleiter** möchte ich **eine Warnung erhalten, wenn ich zu viele oder zu wenige Stunden geplant habe**, damit **ich den Arbeitsplan rechtzeitig korrigieren kann**.

### Beschreibung

Der Schichtleiter darf mehr Stunden planen als von der HR freigegeben wurden, erhält aber eine Warnung. Auch wenn deutlich weniger als das Kontingent geplant ist, soll er darauf hingewiesen werden.

### Akzeptanzkriterien

#### AK-1: Warnung bei Überschreitung

**Given** ein Monatskontingent von 1000 Stunden ist vorhanden  
**When** der Schichtleiter mehr als 1000 Stunden plant  
**Then** zeigt das System eine Warnung zur Überschreitung an

#### AK-2: Warnung bei Unterplanung

**Given** ein Monatskontingent von 1000 Stunden ist vorhanden  
**When** der Schichtleiter weniger als die definierte Mindestmenge plant  
**Then** zeigt das System eine Warnung zur Unterplanung an

#### AK-3: Speichern trotz Warnung erlauben

**Given** der Arbeitsplan überschreitet das Kontingent  
**When** der Schichtleiter den Plan speichern möchte  
**Then** kann der Plan gespeichert werden, aber die Warnung bleibt sichtbar

### Randfälle und Fehlerfälle

- Keine Mindestgrenze definiert
- Kontingent wurde nachträglich von HR geändert
- Warnung wird trotz korrekter Planung angezeigt
- Warnung verschwindet nicht nach Korrektur

### Nicht-funktionale Anforderungen

- Usability: Warnungen dürfen nicht missverständlich sein.
- Nachvollziehbarkeit: Es soll klar sein, wie viele Stunden zu viel oder zu wenig geplant sind.
- Stabilität: Warnungen müssen nach jeder Änderung aktualisiert werden.

### Abhängigkeiten

- Planning Service
- HR-Stundenfreigabe
- Frontend-Validierung

### Offene Fragen

- Ab welcher Untergrenze soll gewarnt werden, z. B. unter 95 Prozent?
- Soll eine Begründung verlangt werden, wenn über dem Kontingent geplant wird?

---

## US-SL-006: Arbeitsplan veröffentlichen

### Userstory

Als **Schichtleiter** möchte ich **einen fertigen Arbeitsplan veröffentlichen**, damit **Mitarbeiter ihre Schichten im mobilen Kalender sehen können**.

### Beschreibung

Ein Arbeitsplan kann zuerst als Entwurf gespeichert und danach veröffentlicht werden. Nach der Veröffentlichung sehen Mitarbeiter ihre Schichten in der Mobile App.

### Akzeptanzkriterien

#### AK-1: Arbeitsplan veröffentlichen

**Given** ein Arbeitsplan enthält gültige Schichten  
**When** der Schichtleiter auf „Veröffentlichen“ klickt  
**Then** wird der Arbeitsplan veröffentlicht

#### AK-2: Schichten für Mitarbeiter sichtbar machen

**Given** ein Arbeitsplan wurde veröffentlicht  
**When** ein Mitarbeiter seine Mobile App öffnet  
**Then** sieht er seine geplanten Schichten im Kalender

#### AK-3: Warnung vor Veröffentlichung anzeigen

**Given** der Arbeitsplan enthält Warnungen wegen Stundenüberschreitung  
**When** der Schichtleiter den Plan veröffentlichen möchte  
**Then** wird er vor der Veröffentlichung nochmals auf die Warnung hingewiesen

### Randfälle und Fehlerfälle

- Arbeitsplan enthält keine Schichten
- Schichten enthalten ungültige Daten
- Veröffentlichung schlägt fehl
- Arbeitsplan wurde bereits veröffentlicht
- Mitarbeiter hat keinen Zugriff auf die Mobile App

### Nicht-funktionale Anforderungen

- Sicherheit: Nur Schichtleiter dürfen ihre Arbeitspläne veröffentlichen.
- Konsistenz: Veröffentlichte Daten müssen im Mitarbeiterkalender korrekt erscheinen.
- Usability: Status Entwurf/Veröffentlicht muss klar sichtbar sein.

### Abhängigkeiten

- Planning Service
- Flutter Mobile App
- Auth Service
- Kalenderansicht der Mitarbeiter

### Offene Fragen

- Können veröffentlichte Pläne nachträglich geändert werden?
- Müssen Mitarbeiter bei Veröffentlichung benachrichtigt werden?

---

## US-SL-007: Aufträge einsehen

### Userstory

Als **Schichtleiter** möchte ich **mir zugewiesene Aufträge einsehen**, damit **ich die Schichten passend zu den Aufträgen planen kann**.

### Beschreibung

Der Schichtleiter sieht Aufträge, die ihm zugewiesen wurden. Dazu gehören relevante Auftragsinformationen wie Name, Zeitraum, Status und zugewiesene Mitarbeiter.

### Akzeptanzkriterien

#### AK-1: Auftragsliste anzeigen

**Given** dem Schichtleiter wurden Aufträge zugewiesen  
**When** er die Auftragsseite öffnet  
**Then** sieht er eine Liste seiner Aufträge

#### AK-2: Auftragsdetails anzeigen

**Given** eine Auftragsliste ist sichtbar  
**When** der Schichtleiter einen Auftrag auswählt  
**Then** sieht er die Details dieses Auftrags

#### AK-3: Nach Status filtern

**Given** mehrere Aufträge mit unterschiedlichen Status existieren  
**When** der Schichtleiter nach Status filtert  
**Then** werden nur passende Aufträge angezeigt

### Randfälle und Fehlerfälle

- Keine Aufträge zugewiesen
- Auftrag wurde gelöscht oder deaktiviert
- Schichtleiter versucht fremde Aufträge zu öffnen
- Auftragsdaten sind unvollständig

### Nicht-funktionale Anforderungen

- Sicherheit: Schichtleiter dürfen nur ihre zugewiesenen Aufträge sehen.
- Usability: Aufträge sollen übersichtlich filterbar sein.
- Performance: Auftragsliste soll auch bei vielen Einträgen schnell laden.

### Abhängigkeiten

- Order Service
- User & Role Service
- Auth Service

### Offene Fragen

- Darf der Schichtleiter Aufträge bearbeiten oder nur ansehen?
- Welche Auftragsdaten sind für den Schichtleiter Pflicht?

---

## US-SL-008: Mitarbeiter einem Auftrag oder einer Schicht zuweisen

### Userstory

Als **Schichtleiter** möchte ich **Mitarbeiter einem Auftrag oder einer Schicht zuweisen**, damit **klar ist, wer wann für welchen Auftrag arbeitet**.

### Beschreibung

Bei der Planung kann der Schichtleiter Mitarbeiter auswählen und einer Schicht oder einem Auftrag zuordnen.

### Akzeptanzkriterien

#### AK-1: Mitarbeiter auswählen

**Given** ein Arbeitsplan oder Auftrag ist geöffnet  
**When** der Schichtleiter eine Schicht erstellt  
**Then** kann er einen verfügbaren Mitarbeiter auswählen

#### AK-2: Mitarbeiter zuweisen

**Given** ein Mitarbeiter wurde ausgewählt  
**When** der Schichtleiter die Zuweisung speichert  
**Then** ist der Mitarbeiter der Schicht oder dem Auftrag zugeordnet

#### AK-3: Zuweisung anzeigen

**Given** ein Mitarbeiter wurde einer Schicht zugewiesen  
**When** der Schichtleiter den Arbeitsplan öffnet  
**Then** sieht er den Mitarbeiter bei der entsprechenden Schicht

### Randfälle und Fehlerfälle

- Mitarbeiter existiert nicht mehr
- Mitarbeiter ist bereits in einer anderen Schicht eingeplant
- Mitarbeiter ist abwesend
- Schichtleiter hat keine Berechtigung für diesen Mitarbeiter

### Nicht-funktionale Anforderungen

- Datenqualität: Doppelbuchungen sollen erkannt werden.
- Sicherheit: Zugriff nur auf erlaubte Mitarbeiter.
- Usability: Mitarbeiterauswahl soll such- und filterbar sein.

### Abhängigkeiten

- Planning Service
- Order Service
- User & Role Service
- Absence & Vacation Service

### Offene Fragen

- Gehören Mitarbeiter fest zu einem Schichtleiter-Team?
- Sollen Qualifikationen oder Rollen der Mitarbeiter berücksichtigt werden?

---

## US-SL-009: Arbeitszeiten der Mitarbeiter einsehen

### Userstory

Als **Schichtleiter** möchte ich **die erfassten Check-in- und Check-out-Zeiten meiner Mitarbeiter sehen**, damit **ich kontrollieren kann, ob die geplanten Schichten eingehalten wurden**.

### Beschreibung

Mitarbeiter stempeln sich per RFID mit dem Handy ein und aus. Der Schichtleiter kann die erfassten Zeiten seiner Mitarbeiter einsehen und mit den geplanten Schichten vergleichen.

### Akzeptanzkriterien

#### AK-1: Tageszeiten anzeigen

**Given** Mitarbeiter haben sich eingecheckt oder ausgecheckt  
**When** der Schichtleiter die Zeitübersicht öffnet  
**Then** sieht er die erfassten Zeiten pro Mitarbeiter

#### AK-2: Monatsstunden anzeigen

**Given** für einen Mitarbeiter existieren Zeiteinträge  
**When** der Schichtleiter einen Monat auswählt  
**Then** sieht er die berechneten Monatsstunden

#### AK-3: Abweichungen erkennen

**Given** eine geplante Schicht und eine tatsächliche Arbeitszeit existieren  
**When** die Zeiten verglichen werden  
**Then** zeigt das System Abweichungen zwischen Planung und Ist-Zeit an

### Randfälle und Fehlerfälle

- Mitarbeiter hat vergessen auszuchecken
- Zeiteintrag fehlt
- Mitarbeiter checkt ausserhalb der geplanten Schicht ein
- RFID-Erfassung ist fehlerhaft
- Zeitdaten können nicht geladen werden

### Nicht-funktionale Anforderungen

- Datenschutz: Schichtleiter sieht nur die Zeiten seiner Mitarbeiter.
- Genauigkeit: Stundenberechnung muss korrekt sein.
- Nachvollziehbarkeit: Abweichungen müssen verständlich dargestellt werden.

### Abhängigkeiten

- Time Service
- Planning Service
- Mobile App Check-in/Check-out
- Auth Service

### Offene Fragen

- Darf der Schichtleiter fehlerhafte Zeiteinträge korrigieren?
- Muss HR Korrekturen bestätigen?

---

## US-SL-010: Pausenverstösse erkennen

### Userstory

Als **Schichtleiter** möchte ich **sehen, wenn Mitarbeiter ihre nötigen Pausen nicht einhalten**, damit **ich rechtzeitig reagieren und die Arbeitsplanung verbessern kann**.

### Beschreibung

Später soll das System prüfen, ob Mitarbeiter gesetzte Pausenregeln einhalten. Der Schichtleiter erhält Hinweise, wenn Pausen fehlen oder zu kurz sind.

### Akzeptanzkriterien

#### AK-1: Pausenverstoss anzeigen

**Given** ein Mitarbeiter hat eine lange Schicht ohne ausreichende Pause gearbeitet  
**When** der Schichtleiter die Zeitübersicht öffnet  
**Then** wird ein Pausenverstoss angezeigt

#### AK-2: Betroffene Schicht anzeigen

**Given** ein Pausenverstoss wurde erkannt  
**When** der Schichtleiter die Meldung öffnet  
**Then** sieht er den betroffenen Mitarbeiter, das Datum und die Schicht

#### AK-3: Kein Verstoss bei korrekter Pause

**Given** ein Mitarbeiter hat die nötige Pause eingehalten  
**When** die Arbeitszeit geprüft wird  
**Then** wird kein Pausenverstoss angezeigt

### Randfälle und Fehlerfälle

- Pausenregel ist nicht definiert
- Check-out fehlt
- Arbeitszeitdaten sind unvollständig
- Mitarbeiter arbeitet über Mitternacht

### Nicht-funktionale Anforderungen

- Korrektheit: Pausenregeln müssen eindeutig berechnet werden.
- Usability: Verstösse müssen klar von normalen Hinweisen unterscheidbar sein.
- Datenschutz: Nur berechtigte Personen dürfen Pausendaten sehen.

### Abhängigkeiten

- Time Service
- Planning Service
- Pausenregeln durch HR oder Admin
- Mobile App Check-in/Check-out

### Offene Fragen

- Welche Pausenregeln gelten genau?
- Soll der Schichtleiter Verstösse kommentieren können?
- Soll HR automatisch benachrichtigt werden?

---

## US-SL-011: Notizen zu Aufträgen oder Schichten erfassen

### Userstory

Als **Schichtleiter** möchte ich **Notizen zu Aufträgen oder Schichten erfassen**, damit **wichtige Informationen für HR, Admin oder das Team dokumentiert sind**.

### Beschreibung

Der Schichtleiter kann Notizen erfassen, zum Beispiel zu besonderen Ereignissen, Planungsproblemen, Personalengpässen oder Auftragsinformationen.

### Akzeptanzkriterien

#### AK-1: Notiz erstellen

**Given** ein Auftrag oder eine Schicht ist geöffnet  
**When** der Schichtleiter eine Notiz schreibt und speichert  
**Then** wird die Notiz gespeichert

#### AK-2: Notiz anzeigen

**Given** eine Notiz wurde gespeichert  
**When** der Schichtleiter den Auftrag oder die Schicht erneut öffnet  
**Then** wird die Notiz angezeigt

#### AK-3: Leere Notiz verhindern

**Given** das Notizfeld ist leer  
**When** der Schichtleiter speichern möchte  
**Then** wird die Notiz nicht gespeichert und eine Meldung angezeigt

### Randfälle und Fehlerfälle

- Notiz ist zu lang
- Notiz enthält ungültige Zeichen
- Speichern schlägt fehl
- Schichtleiter hat keinen Zugriff auf den Auftrag

### Nicht-funktionale Anforderungen

- Usability: Notizen sollen schnell erfassbar sein.
- Sicherheit: Nur berechtigte Benutzer dürfen Notizen sehen oder bearbeiten.
- Nachvollziehbarkeit: Ersteller und Zeitpunkt der Notiz sollen gespeichert werden.

### Abhängigkeiten

- Schichtleiter Web `/notes`
- Order Service oder Planning Service
- Auth Service

### Offene Fragen

- Können Notizen nachträglich bearbeitet oder gelöscht werden?
- Sind Notizen nur für Schichtleiter sichtbar oder auch für HR/Admin?

---

## US-SL-012: Arbeitsplan suchen, filtern und sortieren

### Userstory

Als **Schichtleiter** möchte ich **Arbeitspläne, Schichten und Mitarbeiter suchen, filtern und sortieren**, damit **ich auch bei vielen Daten schnell den richtigen Eintrag finde**.

### Beschreibung

Der Schichtleiter kann seine Planungsdaten nach Monat, Mitarbeiter, Auftrag, Status oder Warnung filtern.

### Akzeptanzkriterien

#### AK-1: Nach Mitarbeiter suchen

**Given** mehrere Mitarbeiter sind eingeplant  
**When** der Schichtleiter nach einem Namen sucht  
**Then** werden nur passende Schichten oder Einträge angezeigt

#### AK-2: Nach Monat filtern

**Given** Arbeitspläne für mehrere Monate existieren  
**When** der Schichtleiter einen Monat auswählt  
**Then** werden nur Arbeitspläne dieses Monats angezeigt

#### AK-3: Nach Warnungen filtern

**Given** einige Arbeitspläne enthalten Warnungen  
**When** der Schichtleiter den Warnungsfilter aktiviert  
**Then** werden nur Arbeitspläne mit Warnungen angezeigt

### Randfälle und Fehlerfälle

- Suche liefert keine Treffer
- Filterkombination ergibt keine Ergebnisse
- Daten können nicht geladen werden
- Suchbegriff enthält ungültige Zeichen

### Nicht-funktionale Anforderungen

- Performance: Suche und Filter sollen schnell reagieren.
- Usability: Aktive Filter müssen sichtbar sein.
- Barrierefreiheit: Filter und Suche müssen klar beschriftet sein.

### Abhängigkeiten

- Planning Service
- Order Service
- Frontend-Komponenten für Suche und Filter

### Offene Fragen

- Welche Filter sind für die erste Version Pflicht?
- Soll die Suche live während der Eingabe funktionieren?

---

# Empfohlene Reihenfolge für die Umsetzung

1. **US-SL-001: Dashboard anzeigen**
2. **US-SL-002: Arbeitsplan erstellen**
3. **US-SL-003: Schichten hinzufügen**
4. **US-SL-004: Geplante Stunden vergleichen**
5. **US-SL-005: Warnungen bei Über-/Unterplanung**
6. **US-SL-006: Arbeitsplan veröffentlichen**
7. **US-SL-007: Aufträge einsehen**
8. **US-SL-009: Arbeitszeiten einsehen**
9. **US-SL-011: Notizen erfassen**
10. **US-SL-010: Pausenverstösse erkennen** als spätere Erweiterung.
