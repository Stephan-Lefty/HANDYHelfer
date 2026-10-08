[Deutsch](README.md) | [English](README.en.md) | [Änderungsprotokoll](#änderungsprotokoll) | [TODO](TODO.md)

<img src="assets/banner.svg#gh-light-mode-only" alt="HANDYHelfer – Hilfe auf Knopfdruck." width="600">
<img src="assets/banner-dark.svg#gh-dark-mode-only" alt="HANDYHelfer – Hilfe auf Knopfdruck." width="600">

# HANDYHelfer

Ein Knopf auf dem Handy, der Hilfe holt. Gedacht für den Fall, dass ein
Angehöriger – der Sohn, die Tochter, der Nachbar – ein Handy aus der Ferne
warten soll, und dass der Weg dorthin bisher an genau den Schritten scheitert,
die der hilfsbedürftige Mensch nicht gehen kann.

Dieses Projekt ist in Zusammenarbeit mit [Claude](https://claude.com) entstanden.

## Das Problem

So läuft es heute: Die Mutter merkt, dass etwas nicht geht. Sie ruft an. Sie
erklärt, so gut es geht. Der Sohn sagt „öffne mal RustDesk". Sie sucht das
Symbol. „Lies mir die Kennung vor." Neun Ziffern. „Und jetzt das Passwort."
Dann drei Systemdialoge, die bestätigt werden müssen.

Genau dort bricht es ab.

## Was HANDYHelfer macht

**Ein Knopf.** Sie tippt darauf, beim Sohn klingelt das Telefon. Die Nummer
steht fest hinterlegt, es gibt nichts auszuwählen und nichts vorzulesen.

**Ein zweiter Knopf** öffnet die Fernhilfe, damit sie nicht gesucht werden muss.

**Ein Zustandsbericht** steht darunter – und zwar in Sätzen, nicht in Zahlen:
„Das Handy ist stumm gestellt." „Der Speicher ist fast voll, nur noch 500 MB
frei." „Seit 15 Tagen kein Neustart."

Der Bericht wird **angezeigt und nicht verschickt**. Das ist kein Mangel,
sondern der Grund, warum die App ohne Server, ohne Konto und ohne eine einzige
Netzwerkverbindung auskommt: Der Helfer sieht den Bildschirm ohnehin, sobald
die Fernhilfe läuft – also liest er ihn dort.

## Was HANDYHelfer nicht macht

**Es steuert nichts fern und sieht keinen Bildschirm.** Das macht
[RustDesk](https://rustdesk.com) – quelloffen unter AGPL-3.0, mit eigenem
Relay-Server betreibbar, seit Jahren gepflegt. Das nachzubauen wären Monate
Arbeit für ein schlechteres Ergebnis.

Es wäre außerdem aussichtslos. Seit Android 14 verlangt `MediaProjection` für
**jede** Sitzung eine neue Zustimmung am Gerät; ein zwischengespeichertes
Intent wirft eine `SecurityException`. Seit Android 15 QPR1 bricht die Aufnahme
ab, sobald der Bildschirm sperrt. Es gibt dafür keine Hintertür – nicht für
Apps, nicht über MDM, nicht als Device Owner. Daran scheitert auch TeamViewer,
und RustDesk schreibt es selbst in seine Dokumentation: *„Treat Android control
as attended support rather than set-and-forget access."*

HANDYHelfer nimmt diese Grenze als gegeben hin und kümmert sich um das, was
davor liegt.

## Der Schutz ist Teil der Bauart

RustDesk allein zeigt der Mutter eine neunstellige Zahl und fragt, ob sie
verbinden will. Sie kann nicht erkennen, ob da ihr Sohn dran ist oder der
freundliche Herr, der vorher angerufen hat und behauptet, von Microsoft zu
sein. Genau diese Masche steht bei [Polizeiberatung][pol] und bei
[Watchlist Internet][wli] namentlich mit RustDesk, AnyDesk und TeamViewer im
Warntext. Laut [IC3-Report 2025][ic3] tragen Menschen über 60 rund 37 % aller
Betrugsschäden.

Deshalb:

- **Genau ein Helfer.** Es gibt kein Eingabefeld für eine fremde Nummer – also
  lässt sich am Telefon auch niemand zu einer überreden.
- **Die Einrichtung erklärt, warum.** Wer die Begründung einmal gelesen hat,
  wird bei genau dieser Bitte stutzig.
- **Ein Protokoll am Gerät**, im Klartext, nicht löschbar aus der App heraus.
- **Keine Sicherung in die Cloud** (`allowBackup="false"`) – der Helfer wandert
  nicht mit auf ein anderes Gerät.

Die Anforderungsliste dafür musste nicht erfunden werden; sie steht seit Jahren
beim [LfDI Baden-Württemberg][lfdi] und im BSI-Baustein OPS.1.2.5.

[pol]: https://www.polizei-beratung.de/aktuelles/detailansicht/tech-support-scams-falsche-microsoft-mitarbeiter-am-telefon/
[wli]: https://www.watchlist-internet.at/news/achtung-bei-anrufen-von-microsoft/
[ic3]: https://www.ic3.gov/AnnualReport/Reports/2025_IC3Report.pdf
[lfdi]: https://www.baden-wuerttemberg.datenschutz.de/fernwartung/

## Zwei Berechtigungen, mehr nicht

| Berechtigung | Wofür | Ohne sie |
|---|---|---|
| `CALL_PHONE` | die hinterlegte Nummer direkt wählen | die Nummer steht im Telefon, es fehlt ein Tipp |
| `ACCESS_NETWORK_STATE` | ob überhaupt eine Verbindung besteht | – (normale Berechtigung, kein Dialog) |

Bewusst **nicht** dabei: `BIND_ACCESSIBILITY_SERVICE`, alles rund um
`MediaProjection`, `QUERY_ALL_PACKAGES`, `REQUEST_INSTALL_PACKAGES`,
Standort, Kontakte, Kamera, Mikrofon.

Damit bleibt die App außerhalb der Play-Regeln zur Accessibility-API, die seit
dem 15.04.2026 jede autonome Aktion darüber verbieten, und außerhalb der
Beschränkungen des Advanced Protection Mode ab Android 17.

Statt `QUERY_ALL_PACKAGES` steht genau ein Paketname unter `<queries>` im
Manifest. Die Berechtigung, die ganze App-Liste zu lesen, braucht bei Google
eine Sondergenehmigung und wäre für diesen Zweck nicht zu rechtfertigen.

## Aufbau

```
MainActivity          der Startbildschirm: zwei Knöpfe, der Bericht
  ├── Diagnose        trägt den Gerätezustand zusammen (Android)
  ├── Regeln          macht daraus Befunde          (rein, testbar)
  ├── BefundTexte     macht daraus Sätze            (Ressourcen)
  ├── Fernhilfe       die Brücke zu RustDesk
  ├── Helfer          der eine hinterlegte Helfer
  └── Protokoll       was wann geschehen ist
```

Die Trennung zwischen `Diagnose` und `Regeln` ist der Kern: `Regeln` kennt
kein Android und keine Zeichenketten, sondern nur die Datenklasse `Zustand`.
Deshalb lässt sich ein fast voller Speicher, ein zwei Wochen nicht neu
gestartetes Gerät oder ein abgelaufenes Android im Test herstellen – am Telefon
wäre das jeweils eine Stunde Arbeit.

## Bauen

```
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

Gebraucht wird ein Android SDK mit API 36 und ein JDK 17.

## Lizenz

Apache 2.0, siehe [LICENSE](LICENSE).

## Änderungsprotokoll

### 0.1.0 – in Arbeit

Erste Fassung. Der Knopf ruft an, der zweite öffnet RustDesk, der
Zustandsbericht prüft dreizehn Dinge. Noch **auf keinem Gerät gelaufen** –
offene Punkte stehen in [TODO.md](TODO.md).
