[Deutsch](README.md) | [English](README.en.md) | [Änderungsprotokoll](#änderungsprotokoll) | [TODO](TODO.md)

<img src="assets/banner.svg#gh-light-mode-only" alt="HANDYHelfer – Hilfe auf Knopfdruck." width="600">
<img src="assets/banner-dark.svg#gh-dark-mode-only" alt="HANDYHelfer – Hilfe auf Knopfdruck." width="600">

# HANDYHelfer

Ein Knopf auf dem Handy, der Hilfe holt. Für den Fall, dass ein Angehöriger –
der Sohn, die Tochter, der Nachbar – ein Handy aus der Ferne warten soll, und
dass der Weg dorthin bisher an genau den Schritten scheitert, die der
hilfsbedürftige Mensch nicht gehen kann.

Dieses Projekt ist in Zusammenarbeit mit [Claude](https://claude.com) entstanden.

## Das Problem

So läuft es heute: Die Mutter merkt, dass etwas nicht geht. Sie ruft an. Sie
erklärt, so gut es geht. Der Sohn sagt „öffne mal die Fernwartung". Sie sucht
das Symbol. „Lies mir die Kennung vor." Neun Ziffern. „Und jetzt das Passwort."
Dann drei Systemdialoge, die bestätigt werden müssen.

Genau dort bricht es ab.

## Was HANDYHelfer macht

**Ein Knopf.** Sie tippt darauf, beim Sohn klingelt das Telefon. Die Nummer
steht fest hinterlegt, es gibt nichts auszuwählen und nichts vorzulesen. Den
Knopf gibt es dreifach: in der App, als bildschirmbreiter Balken auf dem
Startbildschirm („Hilfe von Stephan") und als Kachel in den Schnelleinstellungen
– Letztere erreicht man auch dann, wenn eine fremde App klemmt und der
Startbildschirm außer Reichweite ist.

**Ein zweiter Knopf** öffnet die Fernhilfe, damit sie nicht gesucht werden muss.

**Ein Zustandsbericht** steht darunter, in Sätzen statt in Zahlen: „Das Handy
ist stumm gestellt." „Der Speicher ist fast voll, nur noch 500 MB frei." „Seit
15 Tagen kein Neustart."

Der Bericht wird **angezeigt und nicht verschickt**. Das ist kein Mangel,
sondern der Grund, warum die App ohne Server, ohne Konto und ohne eine einzige
Netzwerkverbindung auskommt: Der Helfer sieht den Bildschirm ohnehin, sobald die
Fernhilfe läuft – also liest er ihn dort.

**Eine Einrichtung, die sich selbst prüft.** Jeder Schritt öffnet die
Systemseite, auf der er stattfindet, statt zu beschreiben, wo ein Schalter
liegt – Menüpfade heißen bei Samsung, Xiaomi und Motorola jeweils anders, ein
Intent trifft überall. Ein Haken steht nur da, wo die App wirklich nachgesehen
hat; ein Fragezeichen heißt, dass sie es nicht kann.

## Was HANDYHelfer nicht macht

**Es steuert nichts fern und sieht keinen Bildschirm.** Das macht ein eigenes
Programm, und HANDYHelfer kennt vier davon.

Das nachzubauen wäre aussichtslos: Seit Android 14 verlangt `MediaProjection`
für **jede** Sitzung eine neue Zustimmung; ein zwischengespeichertes Intent
wirft eine `SecurityException`. Seit Android 15 QPR1 bricht die Aufnahme ab,
sobald der Bildschirm sperrt. Daran scheitert auch TeamViewer.

## Die unangenehme Wahrheit über Fernsteuerung auf Android

Diese Erkenntnis hat einen ganzen Tag am lebenden Gerät gekostet, deshalb steht
sie hier und nicht im Kleingedruckten:

**Quelloffen und wirklich fernsteuern schließen sich derzeit aus.**

Android lässt den Bedienhilfe-Dienst – ohne den niemand aus der Ferne tippen
kann – [nur für Apps aus Google Play zu][google] (bzw. der HUAWEI AppGallery).
Alles andere bekommt den Schalter ausgegraut, mit dem Hinweis *„Gesteuert durch
eingeschränkte Einstellung"*. Auf einem Motorola edge 50 neo mit Android 16
fehlt sogar der Menüpunkt, mit dem sich das aufheben ließe – weder vor noch
nach der Anforderung durch die App, und eine Installation über F-Droid ändert
daran nichts.

Daraus wird eine Zwickmühle, die keine App auflöst:

- **Außerhalb von Play** sperrt Android die Steuerung.
- **Innerhalb von Play** muss der Anbieter Googles Genehmigungsverfahren
  durchlaufen, mit Formular und Demo-Video. Firmen tun das; kleine quelloffene
  Projekte nicht. HopToDesk schreibt deshalb [in der eigenen Dokumentation][htd],
  dass seine Play-Fassung nur zusehen kann.

| Fernhilfe | quelloffen | kann steuern |
|---|---|---|
| RustDesk | ja | nein – aus Play zurückgezogen |
| HopToDesk | ja | nein – darf es in Play nicht |
| TeamViewer QuickSupport | nein | **ja**, mit Universal Add-On |
| AnyDesk | nein | ja, mit Zusatzmodul |

HANDYHelfer sagt das in der Einrichtung offen: Wo nur Zusehen möglich ist,
steht das da – statt eines Schalters, den Android ausgegraut lässt.

[google]: https://support.google.com/android/answer/12623953?hl=DE
[htd]: https://blog.hoptodesk.com/controlling-an-android-tablet-or-phone-unattended-with-hoptodesk

## Der Schutz ist Teil der Bauart

Eine Fernwartung allein zeigt der Mutter eine neunstellige Zahl und fragt, ob
sie verbinden will. Sie kann nicht erkennen, ob da ihr Sohn dran ist oder der
freundliche Herr, der vorher angerufen hat. Genau diese Masche steht bei
[Polizeiberatung][pol] und [Watchlist Internet][wli] namentlich mit RustDesk,
AnyDesk und TeamViewer im Warntext. Laut [IC3-Report 2025][ic3] tragen Menschen
über 60 rund 37 % aller Betrugsschäden. RustDesk hat seine Android-App 2024
**selbst aus dem Play Store genommen**, weil Betrüger sie missbraucht haben.

Deshalb:

- **Genau ein Helfer.** Es gibt kein Eingabefeld für eine fremde Nummer – also
  lässt sich am Telefon auch niemand zu einer überreden.
- **Die Einrichtung erklärt, warum.** Wer die Begründung einmal gelesen hat,
  wird bei genau dieser Bitte stutzig.
- **Ein Protokoll am Gerät**, im Klartext, nicht löschbar aus der App heraus.
- **Keine Sicherung in die Cloud** (`allowBackup="false"`) – der Helfer wandert
  nicht mit auf ein anderes Gerät.

Die Anforderungsliste musste nicht erfunden werden; sie steht seit Jahren beim
[LfDI Baden-Württemberg][lfdi] und im BSI-Baustein OPS.1.2.5.

[pol]: https://www.polizei-beratung.de/aktuelles/detailansicht/tech-support-scams-falsche-microsoft-mitarbeiter-am-telefon/
[wli]: https://www.watchlist-internet.at/news/achtung-bei-anrufen-von-microsoft/
[ic3]: https://www.ic3.gov/AnnualReport/Reports/2025_IC3Report.pdf
[lfdi]: https://www.baden-wuerttemberg.datenschutz.de/fernwartung/

## Drei Berechtigungen, mehr nicht

| Berechtigung | Wofür | Ohne sie |
|---|---|---|
| `CALL_PHONE` | die hinterlegte Nummer direkt wählen | die Nummer steht im Telefon, es fehlt ein Tipp |
| `READ_PHONE_STATE` | bei zwei SIM-Karten die richtige mitgeben | Android fragt bei jedem Anruf nach |
| `ACCESS_NETWORK_STATE` | ob überhaupt eine Verbindung besteht | – (normal, kein Dialog) |

Bewusst **nicht** dabei: `BIND_ACCESSIBILITY_SERVICE`, alles rund um
`MediaProjection`, `QUERY_ALL_PACKAGES`, `REQUEST_INSTALL_PACKAGES`, Standort,
Kontakte, Kamera, Mikrofon.

Statt `QUERY_ALL_PACKAGES` stehen fünf Paketnamen unter `<queries>` im Manifest –
die vier Fernhilfen und das TeamViewer-Zusatzpaket. Die Berechtigung, die ganze
App-Liste zu lesen, braucht bei Google eine Sondergenehmigung und wäre für
diesen Zweck nicht zu rechtfertigen.

## Auf dem Handy installieren

Die fertige Datei liegt bei den [Veröffentlichungen][rel]. Dieser Link zeigt
immer auf die neueste Fassung:

```
https://github.com/Stephan-Lefty/HANDYHelfer/releases/latest/download/HANDYHelfer.apk
```

Auf dem Handy abtippen will den niemand – dafür ist der Code da. Abfotografieren,
und der Browser lädt die Datei:

<img src="assets/installieren-qr.png" alt="Strichcode, der zur neuesten HANDYHelfer-Datei führt" width="180">

Beim ersten Mal fragt Android, ob dieser Browser Programme installieren darf.
Das ist die normale Rückfrage bei allem, was nicht aus dem Play Store kommt.

> **Ein Hinweis, der hier wichtig ist:** Eine so installierte App bekommt von
> Android die Bedienungshilfen gesperrt. Für HANDYHelfer ist das egal – es
> braucht sie nicht. Für die *Fernhilfe* ist es entscheidend, und genau deshalb
> führt die Einrichtung dort in den Play Store. Die Begründung steht weiter oben.

[rel]: https://github.com/Stephan-Lefty/HANDYHelfer/releases

## Selbst bauen

```
./gradlew assembleDebug
```

Die APK liegt danach unter `app/build/outputs/apk/debug/`.

Gebraucht wird ein Android SDK mit API 36 und ein JDK 17 – **nicht** das
Standard-JDK, falls das neuer ist: Gradle 8.14 stolpert über vierteilige
Versionsnummern wie `26.0.2.1`.

## Aufbau

```
MainActivity          der Startbildschirm: zwei Knöpfe, der Bericht
AssistentActivity     die Einrichtung, die sich selbst prüft
  ├── Assistent       welche Schritte offen sind       (rein, testbar)
  ├── Systemseiten    öffnet die richtige Systemseite
  ├── Fernhilfeart    die vier Programme und was sie können
  ├── Diagnose        trägt den Gerätezustand zusammen
  ├── Regeln          macht daraus Befunde             (rein, testbar)
  ├── Karten          welche SIM-Karte den Anruf macht
  ├── Helfer          der eine hinterlegte Helfer
  └── Protokoll       was wann geschehen ist
```

Die Trennung zwischen `Diagnose` und `Regeln` ist der Kern: `Regeln` kennt kein
Android und keine Zeichenketten, sondern nur die Datenklasse `Zustand`. Deshalb
lässt sich ein fast voller Speicher, ein zwei Wochen nicht neu gestartetes Gerät
oder ein abgelaufenes Android im Test herstellen – am Telefon wäre das jeweils
eine Stunde Arbeit.

## Lizenz

Apache 2.0, siehe [LICENSE](LICENSE).

## Änderungsprotokoll

### 0.1.0 – in Arbeit

Erste Fassung, am Gerät erprobt (Motorola edge 50 neo, Android 16). Der Knopf
ruft an, der Zustandsbericht prüft dreizehn Dinge, die Einrichtung führt durch
neun Schritte. Offene Punkte stehen in [TODO.md](TODO.md).
