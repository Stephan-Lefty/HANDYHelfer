# Hinweise für Claude

Dieses Repository ist **HANDYHelfer** – ein Knopf auf dem Handy, der Hilfe holt.
Gedacht für Angehörige, die ein Handy aus der Ferne warten sollen. Stephan
arbeitet allein daran (kein Team) – bitte durchgehend „du" statt „ihr/euch", und
Stephan darf dich gerne „ClaudIA" nennen.

**Lies zuerst [README.md](README.md)** – dort stehen der Funktionsumfang, der
Aufbau und die Begründung für die unangenehmste Entscheidung des Projekts.
Konkrete offene Aufgaben stehen ausschließlich in [TODO.md](TODO.md), damit der
Stand an einer Stelle bleibt.

## Aktueller Stand (2026-10-08)

Version **0.1.0**, erster Tag. 59 Tests, Lint ohne Fehler, Debug-APK ~5,3 MB.
Am Gerät erprobt: Motorola edge 50 neo, Android 16, mit TeamViewer QuickSupport.

**Belegt am Gerät:** Der Knopf wählt ohne Rückfrage nach der SIM-Karte, die
Fernhilfe wird erkannt, die Einrichtung führt durch neun Schritte, beide Widgets
liegen auf dem Startbildschirm, das Protokoll schreibt mit.

**Noch nie geprüft:** ob eine echte Fernsitzung zustande kommt – getestet wurde
bis zum Öffnen der Fernhilfe, nicht darüber hinaus.

## Die Entscheidung, die man nicht neu aufrollen muss

**Quelloffen und wirklich fernsteuern schließen sich auf Android aus.** Das ist
am 2026-10-08 über Stunden am lebenden Gerät durchprobiert worden, nicht
vermutet:

- Android lässt den Bedienhilfe-Dienst nur für Apps aus Google Play zu. Alles
  andere bekommt den Schalter ausgegraut („Gesteuert durch eingeschränkte
  Einstellung").
- Auf dem Testgerät **fehlt der Menüpunkt zum Aufheben vollständig** – weder vor
  noch nach der Anforderung durch die App, und auch nicht über RustDesks eigenen
  Verweis dorthin.
- **F-Droid löst es nicht.** Nachgewiesen mit `installerPackageName=org.fdroid.fdroid`:
  der Schalter blieb gesperrt. Die maßgebliche Regel ist nicht „sitzungsbasiert
  installiert", sondern „aus dem Store".
- Im Play Store wiederum muss die Steuerung weggelassen werden, sofern der
  Anbieter nicht Googles Genehmigungsverfahren durchläuft. HopToDesk sagt das in
  der eigenen Doku.

Deshalb schlägt HANDYHelfer **TeamViewer QuickSupport** vor, obwohl es nicht
quelloffen ist. Die Begründung steht im Quelltext bei `Fernhilfeart.VORSCHLAG` –
wer sie ändern will, ändert dort eine Zeile.

## Zwei Dinge, die beim Bauen Zeit kosten

**Gradle läuft nicht mit dem Standard-JDK.** Das System hat OpenJDK 26, Gradle
8.14 stolpert über dessen vierteilige Versionsnummer:

```
export JAVA_HOME=~/.gradle/jdks/eclipse_adoptium-17-amd64-linux.2
```

**Lints Versionsvorschläge sind hier falsch.** Für alle vier Bibliotheken meldet
Lint eine neuere; `core-ktx 1.19.1` verlangt aber compileSdk 37, während AGP
8.13 höchstens 36 empfiehlt. Ebenso falsch: `mipmap-anydpi-v26` nach
`mipmap-anydpi` umzubenennen – danach findet AAPT das Symbol nicht mehr. Beides
steht als Ausnahme in `app/lint.xml`, mit Datum.

## Was nur ein Gerätetest findet

Fünf Fehler des ersten Tages waren bei grünen Unit-Tests unsichtbar. Sie stehen
hier, weil sie ein Muster bilden:

- Der Inhalt lief **unter die Statusleiste** – ab Android 15 ist Edge-to-Edge
  Pflicht (`Raender.kt`).
- Aus den Unterseiten gab es **keinen Weg zurück**, weil das Theme `NoActionBar`
  war und `setDisplayHomeAsUpEnabled()` ins Leere lief.
- Die Gerätezeile las sich **„Motorola motorola edge 50 neo"** – `Build.MODEL`
  trägt den Hersteller bei Motorola schon, bei Samsung nicht (`geraetName()`).
- Der Anruf blieb in **`SELECT_PHONE_ACCOUNT`** stehen: zwei Karten, keine als
  Standard (`Karten.kt`).
- Der Bedienhilfe-Dienst sitzt bei TeamViewer im **Zusatzpaket**, nicht im
  Hauptpaket – wer nur das Hauptpaket prüft, findet ihn nie.

Die Lehre: Die App darf vor einem Release nicht nur gebaut, sondern muss
**einmal von Hand durchgeklickt** werden.

## Regeln, die nicht verhandelbar sind

**Ein Haken nur, wo wirklich nachgesehen wurde.** Was die App nicht prüfen kann
(festes Passwort, App-Pause fremder Programme), trägt ein Fragezeichen und sagt
das im Text. Ein Haken, der nichts belegt, ist schlimmer als keiner.

**Genau ein Helfer, kein Eingabefeld für fremde Nummern.** Das ist der Kern des
Missbrauchsschutzes, nicht Sparsamkeit. Wer daran etwas ändert, hebelt den Zweck
der App aus.

**Keine echten Rufnummern im Repo.** Beispiele kommen aus den „Drama Numbers"
der Bundesnetzagentur (Mitteilung 148/2021) – dauerhaft niemandem zugeteilt.

**Farbe trägt keine Bedeutung allein.** Haken, Kreis und Fragezeichen
unterscheiden sich als Zeichen, nicht nur als Farbe.

## Wo was steht

| Datei | Wofür |
|---|---|
| `Assistent.kt` | welche Einrichtungsschritte offen sind – rein, testbar |
| `Befund.kt` | `Zustand`, `Regeln`, `geraetName()` – rein, testbar |
| `Diagnose.kt` | liest den Gerätezustand aus Android |
| `Fernhilfeart.kt` | die vier Fernhilfen und was sie können |
| `Systemseiten.kt` | öffnet die Systemseite, auf der ein Schritt stattfindet |
| `Karten.kt` | welche SIM-Karte den Anruf macht |
| `Rufnummer.kt` | Aufbereitung und Mängelbericht – rein, testbar |
| `Protokoll.kt` | was wann geschehen ist, am Gerät |
| `assets/farben.md` | die gemeinsame Palette aller Programme |

## Tests

```
./gradlew testDebugUnitTest
```

Alle Tests laufen ohne Gerät und ohne Robolectric. Das ist der Grund für die
Trennung in reine Datenklassen: Ein fast voller Speicher oder ein Gerät mit zwei
SIM-Karten lässt sich am Telefon kaum herstellen, in einer Datenklasse in einer
Zeile.
