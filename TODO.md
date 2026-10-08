[Deutsch](TODO.md) | [English](TODO.en.md)

# TODO – HANDYHelfer

> **Stand 08.10.2026.** Erster Tag. Das Gerüst steht, 28 Tests grün, Lint ohne
> Fehler, Debug-APK gebaut (5,3 MB). Inzwischen 33 Tests.
>
> **Am Gerät gelaufen** – Motorola edge 50 neo, Android 16, am 08.10.2026.
> Der Startbildschirm steht, der Zustandsbericht erkennt die vergrößerte
> Schrift, RustDesk wird gefunden. Drei Fehler hat erst das Telefon gezeigt;
> sie sind behoben und stehen unten unter „Erledigt".

## Dringend

### Die Eingabesteuerung von RustDesk ist nicht eingeschaltet

Am 08.10. am Gerät festgestellt: `enabled_accessibility_services` ist **leer**.
RustDesk 1.5.0 läuft, kann aber nur den Bildschirm zeigen – tippen und wischen
kann der Helfer nicht.

Der Grund steht wahrscheinlich in der Installationsart:
`installerPackageName=com.google.android.packageinstaller`, also der Weg über
den Browser. Bei so installierten Apps sperrt Android seit Version 13 den
Schalter unter den Bedienungshilfen („eingeschränkte Einstellungen", ab
Android 15 „Enhanced Confirmation Mode"). Aufheben lässt sich das **nur am
Gerät**: App-Info → Drei-Punkte-Menü → eingeschränkte Einstellungen zulassen.
Danach erst lässt sich der Dienst einschalten.

Zu prüfen: ob der Schalter nach dem Freigeben greift, und ob eine Installation
über F-Droid (die session-basiert installiert) das Problem gar nicht erst hat.
Das entscheidet, was in der Einrichtungsanleitung steht.

### Zweimal am Gerät nachsehen

Die beiden Fragen, von denen abhängt, ob das Ganze trägt – beide nur am Telefon
zu beantworten:

1. **Kommt die Eingabesteuerung auf dem Zielhandy durch**, oder darf der Helfer
   nur zusehen? Es gibt einen Bericht, dass Samsungs One UI 8.5 auf Android 16
   sie ganz blockiert ([Issue #15723](https://github.com/rustdesk/rustdesk/issues/15723)).
   Trifft das zu, ist HANDYHelfer auf solchen Geräten auf Anruf und
   Zustandsbericht reduziert – immer noch nützlich, aber die Hälfte fehlt.
2. **Lässt sich RustDesk von außen ansprechen?** Gibt es ein
   `rustdesk://`-Schema, mit dem der Helfer aus einer Benachrichtigung heraus
   direkt zu einer bekannten Kennung verbindet? Und lässt sich die Gegenseite
   per Intent in den Empfangsmodus bringen, statt dass die Betroffene die vier
   Schalter selbst setzt? Beides würde den Ablauf spürbar verkürzen. Die
   Prüfung lief am 08.10. ins Sitzungslimit der Websuche und hat **nichts**
   geliefert – sie ist noch offen, nicht etwa negativ beantwortet.

## Offen

### „Nicht stören" ist oft nicht feststellbar

`Diagnose.nichtStoeren` fragt `NotificationManager.currentInterruptionFilter`.
Ohne Zugriff auf die Benachrichtigungsrichtlinie antwortet Android mit
`INTERRUPTION_FILTER_UNKNOWN`, und das wird bewusst als „nicht feststellbar"
behandelt statt als „aus" – eine Warnung auf Verdacht wäre schlimmer als keine.

Folge: Genau der Befund, der einen der häufigsten Fälle erklärt („Mama geht
nicht ans Telefon"), fehlt am häufigsten. Ob sich das mit
`ACCESS_NOTIFICATION_POLICY` lösen lässt, ohne dass Play Rückfragen stellt, ist
nicht geprüft. Am Gerät nachmessen, was der Filter ohne Berechtigung
tatsächlich zurückgibt – vielleicht ist die Sorge unbegründet.

### Der Helfer erfährt nichts, solange er nicht abnimmt

Der Weckruf ist heute ein Anruf, mehr nicht. Das ist bewusst so angefangen: Es
braucht keinen Server, kein Firebase, kein Konto, und es funktioniert in jedem
Netz. Für „Sohn hilft Mutter" reicht es, weil man dabei ohnehin telefoniert.

Was fehlt, merkt man erst, wenn der Sohn nicht rangeht: Dann weiß er nicht, dass
jemand ihn gebraucht hat. Eine Nachricht hinterherzuschicken ginge ohne
Berechtigung über `ACTION_SENDTO` mit `smsto:` – die SMS-App öffnet sich
vorbefüllt, es fehlt ein Tipp auf Senden. Erst bauen, wenn der Fall wirklich
auftritt.

### Vor einer Veröffentlichung

- **Screenshots** vervollständigen. Vorhanden sind Startseite und Einrichtung;
  Protokoll und Hilfe fehlen. Dabei ist am 08.10. etwas schiefgegangen, das
  sich wiederholen wird: Der Bildschirm ging zwischen den Aufnahmen zu, und
  vier Dateien zeigten den **Sperrbildschirm mit einem privaten Foto** statt
  der App. Sie sind gelöscht und waren nie in einem Commit. Die Lehre: Bei
  Aufnahmen per `adb` jedes Bild einzeln ansehen, bevor es ins Repo wandert –
  `screencap` fotografiert, was da ist, nicht was gemeint war. Nötig ist ein
  entsperrtes Gerät; `svc power stayon usb` hält den Bildschirm an, aber die
  Sperre selbst bleibt. Danach mit Alternativtexten in die READMEs, wie in
  DialOS Mobil.
- **Datenschutzerklärung** (`PRIVACY.md` + `PRIVACY.en.md`). Sie wird kurz: Die
  App erhebt nichts, überträgt nichts und hat keine Netzwerkverbindung.
- **Play-Eintrag**: `CALL_PHONE` braucht keine Sondergenehmigung, aber der
  Eintrag muss erklären, wofür. Entwicklerkonto hängt an
  info@stephanphoto.berlin, Kontoplatz u/2.
- **Signaturschlüssel** anlegen (`handyhelfer-release.jks`), Anleitung steht im
  Kopf von `app/build.gradle.kts`.
- Klären, ob HANDYHelfer **auf dialos.org** erwähnt wird oder eine eigene Seite
  bekommt. Es ist kein Barrierefreiheits-Werkzeug – das trennt es von DialOS,
  obwohl es im selben Paketnamensraum liegt.

## Erledigt

- **08.10.2026, am Gerät** – Drei Fehler, die der Rechner nicht zeigen konnte:
  Der Inhalt lief **unter die Statusleiste** (ab Android 15 zeichnet jede App
  mit targetSdk ≥ 35 zwingend edge-to-edge; behoben in `Raender.kt`). Es gab
  **keinen Weg zurück** aus den Unterseiten, weil das Theme `NoActionBar` war
  und `setDisplayHomeAsUpEnabled()` damit ins Leere lief. Und die Gerätezeile
  las sich **„Motorola motorola edge 50 neo"** – `Build.MODEL` trägt den
  Hersteller bei Motorola schon, bei Samsung dagegen nicht (`SM-A146P`).
  Letzteres steckt jetzt in `geraetName()` mit fünf Testfällen; einer davon
  hat prompt gefunden, dass „OnePlus" zu „Oneplus" wurde.

  Außerdem belegt: Der Paketname `com.carriez.flutter_hbb` stimmt (RustDesk
  1.5.0 auf dem Gerät), und `<queries>` greift – die App findet RustDesk, ohne
  `QUERY_ALL_PACKAGES` zu verlangen.

- **08.10.2026** – Repo angelegt. Gerüst nach dem Muster von DialOS Mobil
  (Kotlin, compileSdk 36, minSdk 26, JDK 17). Icon aus Stephans Vorlage auf die
  gemeinsame Palette umgefärbt, dazu eine Fassung ohne Schriftzug für den
  Startbildschirm und ein Banner nach dem Muster von POSTKutsche. Knopf,
  Helferverwaltung, Zustandsbericht mit dreizehn Regeln, Protokoll, Hilfeseite –
  zweisprachig. 28 Tests.

## Zwei Dinge, die nicht wieder aufzurollen sind

**Gradle läuft hier nicht mit dem Standard-JDK.** Das System hat OpenJDK 26, und
Gradle 8.14 stolpert über dessen vierteilige Versionsnummer
(`IllegalArgumentException: 26.0.2.1`). Vor dem Bauen:

```
export JAVA_HOME=/home/stephan/.gradle/jdks/eclipse_adoptium-17-amd64-linux.2
```

**Lints Versionsvorschläge sind hier falsch.** Für alle vier Bibliotheken meldet
Lint eine neuere; `core-ktx 1.19.1` verlangt aber compileSdk 37, während AGP
8.13 höchstens 36 empfiehlt und API 37 im SDK gar nicht liegt. Am 08.10.
ausprobiert, Build bricht ab. Die Begründung steht im Kopf des
`dependencies`-Blocks. Ebenso falsch: der Vorschlag, `mipmap-anydpi-v26` nach
`mipmap-anydpi` umzubenennen – danach findet AAPT das Symbol nicht mehr. Beide
Fälle stehen als Ausnahme in `app/lint.xml`, mit Datum.
