# OWASP Top 10 Testplan

## Ziel

Diese Dokumentation beschreibt, wie Planifywork anhand der OWASP Top 10 geprüft wird. Ziel ist es, typische Sicherheitsrisiken bei Login, Rollen, API-Endpunkten, Datenbanken, Eingaben und Konfiguration früh zu erkennen und nachvollziehbar zu dokumentieren.

## Scope

Was wird getestet:

* Login und Registrierung
* Authentifizierung und Autorisierung
* API-Endpunkte
* Eingabevalidierung
* Datenbankzugriffe
* Fehlermeldungen
* Sicherheitskonfiguration
* Abhängigkeiten

Was wird nicht getestet:

* Fremde Systeme
* DoS oder Lasttests
* Produktive Daten
* Social Engineering

## Testgrundlage

Als fachliche Grundlage dienen die OWASP Top 10 und der OWASP Web Security Testing Guide. Die OWASP-Projektseite nennt OWASP Top 10:2025 als aktuelle veröffentlichte Version. Für diesen schulischen Testplan wird trotzdem OWASP Top 10:2021 verwendet, weil die Aufgabenstellung diese Kategorien ausdrücklich vorgibt und sie im Schulkontext oft erwartet werden.

Grundlagen:

* OWASP Top 10:2021: https://owasp.org/Top10/2021/
* OWASP Top Ten Projektseite: https://owasp.org/www-project-top-ten/
* OWASP Web Security Testing Guide: https://owasp.org/www-project-web-security-testing-guide/

## User Story

Als Entwickler möchte ich die Webapplikation anhand der OWASP Top 10 prüfen, damit typische Sicherheitsrisiken erkannt, dokumentiert und verbessert werden können.

## Akzeptanzkriterien

* Für relevante OWASP-Kategorien sind passende Testfälle dokumentiert.
* Jeder Testfall enthält Ziel, Vorgehen, Erwartung und Ergebnis.
* Gefundene Risiken werden mit Verbesserungsvorschlag festgehalten.
* Bereits umgesetzte Schutzmassnahmen werden dokumentiert.
* Offene Punkte sind klar markiert.

## Analysierte Projektbereiche

Für diesen Testplan wurden lokal folgende Bereiche gelesen:

* `README.md`
* `docker-compose.yml`
* Backend-Services unter `backend/`
* API-Gateway-Routen und CORS-Konfiguration
* Auth-Code, JWT-Code, SecurityConfig-Klassen und Controller
* DTOs, Services, Repositorys und Datenbankschema
* React-Frontends `admin-web`, `hr-web`, `shiftlead-web`
* Flutter-Mobile-App unter `mobile/`
* API-Testskript `tests/api-test.js`

Sicherheitsrelevante Funktionen:

* Login über `POST /api/auth/login`
* JWT-Erstellung und JWT-Prüfung
* Rollen `ADMIN`, `HR`, `SHIFT_LEAD`, `EMPLOYEE`
* Benutzerverwaltung über `/api/users`
* Planungs-, Zeit-, Absenz- und Rechnungsendpunkte
* MySQL-Zugriffe per Spring Data JPA
* MongoDB-Schema für Medienberichte
* Token-Speicherung im Browser und in der Mobile-App
* CORS-Konfiguration im API-Gateway
* lokale Admin-Oberflächen phpMyAdmin und Mongo Express

## Durchgeführte lokale Prüfungen

Folgende Prüfungen wurden in diesem Arbeitsschritt durchgeführt:

* Code-Review der sicherheitsrelevanten Backend- und Frontend-Dateien.
* Suche nach Rollenprüfungen, JWT-Nutzung, Passwort-Hashing, CORS, Secrets, Validierung und direktem SQL.
* Backend-Verifikation: `mvn -q -pl backend/auth-service,backend/user-role-service -am test` war erfolgreich.
* Frontend-Verifikation: `npm run build` war für `hr-web` und `shiftlead-web` erfolgreich.
* `npm ci` in `hr-web` meldete `found 0 vulnerabilities`.
* `npm install --no-package-lock` in `shiftlead-web` meldete `2 moderate severity vulnerabilities`.

Nicht durchgeführt:

* `node api-test.js` wurde in diesem Arbeitsschritt nicht gegen einen laufenden Docker-Stack erneut ausgeführt.
* Kein DoS, kein Brute Force, keine Tests gegen fremde Systeme.
* Kein produktiver Betrieb und keine produktiven Daten.

## Direkt umgesetzte Verbesserungen

Im Rahmen dieser Prüfung wurden kleine sichere Verbesserungen direkt umgesetzt:

* Login nutzt jetzt ein validiertes DTO statt einer freien `Map`.
* Leere oder zu lange Login-Felder werden mit HTTP 400 abgewiesen.
* `/api/auth/validate` ist für die manuelle Token-Prüfung freigegeben und behandelt fehlende Bearer-Header sauber mit HTTP 401.
* Deaktivierte Konten liefern beim Login keine eigene Detailmeldung mehr, sondern dieselbe generische Meldung wie falsche Logindaten.
* Auth-Service und User-Service geben bei internen Fehlern keine ungefilterte Exception-Message mehr zurück.
* User-Erstellung und User-Update validieren Benutzername, E-Mail, Passwortlänge, Namen und Rollenwerte.
* HR-Web und Schichtleiter-Web prüfen jetzt auch in der Protected Route die gespeicherte Rolle.
* HR-Web speichert und entfernt beim Login/Logout die Rollendaten konsistent.

## OWASP Top 10 Testfälle

| OWASP Kategorie | Risiko für diese App | Testfall | Erwartetes Ergebnis | Tatsächliches Ergebnis | Status | Verbesserung |
|---|---|---|---|---|---|---|
| A01 Broken Access Control | Benutzer könnten Endpunkte oder Datensätze anderer Rollen abrufen. | Code-Review der `@PreAuthorize`-Regeln in User, Planning, Time, Absence und Billing. | Geschützte Endpunkte verlangen JWT und passende Rolle. | Viele Endpunkte nutzen `@PreAuthorize`; z.B. Billing nur HR/Admin, Time-Auswertung nur HR/Admin. User-Liste ist für SHIFT_LEAD auf EMPLOYEE eingeschränkt. | Teilweise bestanden | Objektbezogene Rechte noch ergänzen, z.B. Mitarbeiter darf nur eigene Kalender-, Zeit- und Absenzdaten abrufen. |
| A01 Broken Access Control | Frontend könnte mit altem Token oder falscher Rolle Seiten öffnen. | Review der Protected Routes in den React-Apps. | Frontend prüft Token und erwartete Rolle. | Admin prüfte die Rolle bereits. HR und Schichtleiter wurden ergänzt und prüfen jetzt `HR` bzw. `SHIFT_LEAD`. | Verbessert | Backend bleibt entscheidend; Frontend-Prüfung ist nur Zusatzschutz und UX-Schutz. |
| A01 Broken Access Control | API-Gateway routet weiter, erzwingt aber selbst keine zentrale JWT-Prüfung. | Review von `backend/api-gateway/src/main/resources/application.yml`. | Entweder Gateway oder Zielservice erzwingt Authentifizierung. | Gateway enthält Routen und CORS, aber keinen eigenen JWT-Filter. Zielservices erzwingen Authentifizierung für implementierte Endpunkte. | Teilweise bestanden | Optional Gateway-JWT-Filter ergänzen oder direkte Service-Ports im Dev-Setup klar als intern dokumentieren. |
| A02 Cryptographic Failures | Passwörter könnten im Klartext gespeichert werden. | Review von `PasswordEncoder`, Seed-Code und User-Erstellung. | Passwörter werden gehasht gespeichert. | BCrypt wird in Auth- und User-Service genutzt. Neue User werden mit `passwordEncoder.encode(...)` gespeichert. Seed-User werden per `CommandLineRunner` mit BCrypt erzeugt. | Bestanden | Keine Änderung nötig; Passwortregeln können später stärker werden. |
| A02 Cryptographic Failures | JWT-Secret ist als lokaler Beispielwert im Code enthalten. | Suche nach `jwt.secret` und Secret-Werten. | Secrets kommen aus Umgebung und sind nicht produktiv hardcodiert. | Mehrere `application.yml` enthalten `workforce-super-secret-jwt-key-change-in-production-min-256-bits`. Für lokale Entwicklung erkennbar, für Produktion ungeeignet. | Noch offen | `.env.example` und echte Umgebungsvariablen für Secrets einführen; produktive Secrets nie committen. |
| A02 Cryptographic Failures | Tokens könnten im Browser ausgelesen werden. | Review der Token-Speicherung in Frontend und Mobile. | Tokens werden möglichst geschützt gespeichert. | Web-Frontends speichern JWTs in `localStorage`; Mobile nutzt `SharedPreferences`. Keine Cookies im Einsatz. | Noch offen | XSS-Prävention konsequent halten; optional httpOnly-Cookie-Ansatz oder sichere Mobile-Storage-Lösung prüfen. |
| A03 Injection | SQL Injection über Filter, Suchfelder oder IDs. | Review von Repositorys und Queries. | Keine SQL-Strings aus Benutzereingaben zusammenbauen. | Spring Data JPA und JPQL werden genutzt. Es wurden keine dynamischen SQL-Strings, `Statement` oder `JdbcTemplate`-SQL aus Eingaben gefunden. | Bestanden per Code-Review | Weiterhin keine String-Konkatenation für Queries verwenden. |
| A03 Injection | Enum- oder Eingabefehler könnten Exceptions auslösen. | Review von `valueOf(...)` in Absence und Billing. | Ungültige Enum-Werte werden kontrolliert als 400 behandelt. | Mehrere Services nutzen `Enum.valueOf(...)`; teils wird daraus ein Bad Request, teils können Fehlermeldungen direkt weitergegeben werden. | Teilweise bestanden | DTO-Validierung auch in Absence, Billing, Time und Planning ergänzen. |
| A04 Insecure Design | User-IDs aus Request-Body oder URL können missbraucht werden. | Review von `/api/time/checkin`, `/api/planning/calendar/{employeeId}` und `/api/absences/{id}`. | Die angemeldete Identität wird mit der angefragten ID abgeglichen. | JWT enthält Username und Rolle, aber objektbezogene Checks gegen `employeeId` wurden nicht durchgehend gefunden. | Noch offen | User-ID in JWT aufnehmen oder über Username auflösen und in Services gegen Pfad-/Body-ID prüfen. |
| A04 Insecure Design | Brute Force gegen Login. | Nur Code-Review, kein Brute-Force-Test. | Rate Limiting oder Lockout schützt Login. | Kein Rate Limiting, keine Login-Sperre und kein Zähler für fehlgeschlagene Logins gefunden. | Noch offen | Lokales Rate Limiting am Gateway oder Auth-Service ergänzen. Kein Brute-Force-Test durchgeführt. |
| A05 Security Misconfiguration | CORS könnte zu offen sein. | Review der Gateway-CORS-Konfiguration. | Nur lokale Frontend-Ursprünge sind erlaubt. | Gateway erlaubt nur `localhost:5173`, `3001`, `3002`, `3003` und keine Wildcard-Origin. `allowCredentials` ist aktiv. | Teilweise bestanden | Für Produktion Origins per Umgebung konfigurieren; doppelte CORS-Regeln in Services vereinheitlichen. |
| A05 Security Misconfiguration | Lokale Admin-Oberflächen sind offen erreichbar. | Review von Docker Compose. | Admin-Oberflächen sind nur lokal und nach Möglichkeit geschützt. | phpMyAdmin ist mit DB-User konfiguriert. Mongo Express hat `ME_CONFIG_BASICAUTH: "false"`. | Noch offen | Mongo Express nur lokal verwenden oder Basic Auth aktivieren. In Produktion nicht exponieren. |
| A05 Security Misconfiguration | Detailfehler könnten interne Informationen zeigen. | Review der GlobalExceptionHandler. | Interne Fehler geben keine Stacktraces oder internen Details aus. | Auth- und User-Service wurden verbessert. Andere Services geben teilweise `ex.getMessage()` zurück, auch bei internen Fehlern. | Teilweise verbessert | Einheitliche Fehlerhandler für alle Services einführen. |
| A06 Vulnerable and Outdated Components | Veraltete oder verwundbare Abhängigkeiten. | Lokale npm-Installation/Audit-Hinweise bei HR und Shiftlead; Maven nur Build, kein Vulnerability-Scan. | Keine bekannten kritischen Abhängigkeiten. | `hr-web` meldete 0 npm-Vulnerabilities. `shiftlead-web` meldete 2 moderate npm-Vulnerabilities. Maven Dependency-Check wurde nicht ausgeführt. | Teilweise getestet | `npm audit` pro Frontend und OWASP Dependency-Check oder vergleichbares Tool für Maven ergänzen. |
| A06 Vulnerable and Outdated Components | Docker Images mit `latest` können unerwartet wechseln. | Review von `docker-compose.yml`. | Images sind nachvollziehbar versioniert. | MySQL und MongoDB sind versioniert. phpMyAdmin und mongo-express nutzen `latest`. | Noch offen | Images für reproduzierbare Builds pinnen. |
| A07 Identification and Authentication Failures | Login akzeptiert leere oder sehr lange Felder. | Code-Review und Umsetzung einer kleinen Verbesserung. | Login-Request wird serverseitig validiert. | Auth-Service nutzt jetzt `LoginRequest` mit `@NotBlank` und `@Size`. | Verbessert | Weitere Auth-Tests für leere Felder in `tests/api-test.js` ergänzen. |
| A07 Identification and Authentication Failures | Deaktivierte Konten könnten beim Login erkannt werden. | Review und Umsetzung im Auth-Service. | Login-Fehler geben keine unnötigen Kontodetails preis. | Deaktivierte Konten liefern jetzt dieselbe Meldung wie falsche Logindaten. | Verbessert | Optional Security-Logging intern ergänzen, ohne Details an Client zu senden. |
| A07 Identification and Authentication Failures | Token-Validierung war durch SecurityConfig praktisch nicht sauber erreichbar. | Review und Umsetzung bei `/api/auth/validate`. | Gültiger Bearer-Token kann validiert werden; fehlender Token ergibt 401. | `/api/auth/validate` ist jetzt `permitAll`, prüft Bearer-Header selbst und gibt bei fehlendem/ungültigem Token 401 zurück. | Verbessert | API-Test für Validate-Endpunkt ergänzen. |
| A08 Software and Data Integrity Failures | Frontend-Abhängigkeiten ohne Lockfile sind weniger reproduzierbar. | Review der Frontend-Projekte. | Dependency-Versionen sind über Lockfiles nachvollziehbar. | `hr-web` hat ein `package-lock.json`. Für andere Frontends ist in der Dateiliste kein Lockfile vorhanden. | Noch offen | Lockfiles für alle Frontends erzeugen und committen oder bewusst im Projektstandard dokumentieren. |
| A08 Software and Data Integrity Failures | CI prüft Security nicht automatisch. | Review von Tests und Doku. | Security-Checks laufen automatisiert oder sind dokumentiert. | API-Testskript ist vorhanden, aber kein automatischer Dependency- oder OWASP-Test im Projekt gefunden. | Noch offen | Security-Testschritte in CI aufnehmen, z.B. npm audit und Dependency-Scan. |
| A09 Security Logging and Monitoring Failures | Sicherheitsereignisse werden nicht zentral protokolliert. | Suche nach Security-Logging, Audit oder Login-Failure-Logging. | Login-Fehler, Rollenverstösse und kritische Änderungen werden protokolliert. | Kein zentrales Security-Logging gefunden. Admin-Audit-Log ist lokal im Frontend-State und nicht belastbar. | Noch offen | Server-seitiges Audit-Logging für Login, Rollenänderungen, Deaktivierungen und kritische Schreibzugriffe ergänzen. |
| A10 Server-Side Request Forgery (SSRF) | SSRF über serverseitige URL-Aufrufe. | Code-Review nach externen URL-Requests und frei steuerbaren Server-Requests. | Keine serverseitigen Requests auf vom Benutzer gelieferte URLs. | Keine implementierten Endpunkte gefunden, die frei URLs abrufen. Report/Media-Service hat noch keine Upload-Controller-Logik. | Nicht relevant / Nicht getestet | Bei späteren Uploads oder externen Integrationen allowlist und URL-Validierung ergänzen. |

## Offene Punkte

* Objektbezogene Autorisierung fehlt teilweise noch, besonders bei `employeeId` in URL oder Request-Body.
* Secrets und lokale Datenbankpasswörter stehen als Entwicklungswerte in YAML und Compose.
* Mongo Express ist lokal ohne Basic Auth konfiguriert.
* Weitere Services brauchen einheitliche DTO-Validierung und sichere Fehlerantworten.
* Dependency-Security-Scan für Maven wurde nicht durchgeführt.
* `shiftlead-web` meldete 2 moderate npm-Vulnerabilities.
* API-Security-Negativtests fehlen noch im automatischen Testskript.
* Kein zentrales Security-Logging vorhanden.

## Empfohlene nächste Tests

* `cd tests` und `node api-test.js`, wenn der Docker-Stack läuft.
* Negativtests ergänzen: leerer Login, ungültige E-Mail, ungültige Rolle, zu kurzes Passwort.
* IDOR-Tests ergänzen: Mitarbeiter versucht Kalender, Absenz oder Zeitdaten eines anderen Mitarbeiters abzurufen.
* Dependency-Scan für Java und alle Frontends ergänzen.
* CORS-Test lokal mit erlaubtem und nicht erlaubtem Origin durchführen.
