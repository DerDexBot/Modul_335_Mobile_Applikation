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

## OWASP Top 10 Testfälle

Die konkreten Testfälle und Ergebnisse werden nach der Codeanalyse ergänzt.
