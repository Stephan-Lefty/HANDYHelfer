[Deutsch](README.md) | [English](README.en.md) | [Changelog](#changelog) | [TODO](TODO.en.md)

<img src="assets/banner.svg#gh-light-mode-only" alt="HANDYHelfer – help at the push of a button." width="600">
<img src="assets/banner-dark.svg#gh-dark-mode-only" alt="HANDYHelfer – help at the push of a button." width="600">

# HANDYHelfer

A button on the phone that fetches help. For the case where a relative — a son,
a daughter, a neighbour — is supposed to maintain a phone remotely, and where
the road there has so far failed at exactly the steps the person needing help
cannot take.

This project was built together with [Claude](https://claude.com).

## The problem

Here is how it goes today: Mother notices something is not working. She calls.
She explains as best she can. The son says "open the remote support app". She
looks for the icon. "Read me the ID." Nine digits. "And now the password." Then
three system dialogs to confirm.

That is exactly where it breaks down.

## What HANDYHelfer does

**One button.** She taps it, the son's phone rings. The number is stored for
good; there is nothing to pick and nothing to read out. The button exists three
times over: in the app, as a full-width bar on the home screen ("Help from
Stephan"), and as a tile in the quick settings — the last one is reachable even
when some other app is stuck and the home screen is out of reach.

**A second button** opens the remote helper so it does not have to be found.

**A status report** sits below, in sentences rather than numbers: "The phone is
silenced." "Storage is nearly full, only 500 MB left." "No restart for 15 days."

The report is **displayed, not transmitted**. That is not a shortcoming but the
reason the app needs no server, no account and not a single network connection:
the helper sees the screen anyway once the remote session runs — so that is
where they read it.

**A setup that checks itself.** Each step opens the system page where it takes
place instead of describing where a switch lives — menu paths differ between
Samsung, Xiaomi and Motorola, an intent hits everywhere. A tick only appears
where the app really looked; a question mark means it cannot.

## What HANDYHelfer does not do

**It controls nothing remotely and sees no screen.** A separate program does
that, and HANDYHelfer knows four of them.

Rebuilding it would be hopeless: since Android 14 `MediaProjection` demands
fresh consent for **every** session; a cached intent throws a
`SecurityException`. Since Android 15 QPR1 the capture stops as soon as the
screen locks. TeamViewer fails at this too.

## The uncomfortable truth about remote control on Android

This insight cost a full day on a real device, so it belongs here rather than in
the fine print:

**Open source and real remote control are currently mutually exclusive.**

Android allows the accessibility service — without which nobody can tap from
afar — [only for apps from Google Play][google] (or the HUAWEI AppGallery).
Everything else gets the switch greyed out, marked as controlled by a restricted
setting. On a Motorola edge 50 neo running Android 16 the menu entry to lift
that is missing entirely — neither before nor after the app requests the
permission, and installing via F-Droid changes nothing.

That becomes a bind no app can undo:

- **Outside Play**, Android blocks control.
- **Inside Play**, the vendor must pass Google's approval process with a form
  and a demo video. Companies do that; small open source projects do not.
  HopToDesk therefore states [in its own documentation][htd] that its Play build
  is view-only.

| Remote helper | open source | can control |
|---|---|---|
| RustDesk | yes | no — withdrawn from Play |
| HopToDesk | yes | no — not allowed to in Play |
| TeamViewer QuickSupport | no | **yes**, with Universal Add-On |
| AnyDesk | no | yes, with its add-on |

HANDYHelfer says so openly during setup: where only watching is possible, it
says that — instead of offering a switch Android leaves greyed out.

[google]: https://support.google.com/android/answer/12623953
[htd]: https://blog.hoptodesk.com/controlling-an-android-tablet-or-phone-unattended-with-hoptodesk

## Protection is part of the design

Remote support software on its own shows mother a nine-digit number and asks
whether she wants to connect. She cannot tell whether that is her son or the
friendly man who called earlier. That exact scam is named with RustDesk, AnyDesk
and TeamViewer in warnings from [German police][pol] and [Watchlist
Internet][wli]. According to the [IC3 report 2025][ic3], people over 60 carry
roughly 37 % of all fraud losses. RustDesk **pulled its own Android app from the
Play Store** in 2024 because scammers abused it.

Therefore:

- **Exactly one helper.** There is no input field for a different number — so
  nobody can be talked into one over the phone.
- **Setup explains why.** Anyone who has read the reasoning once will grow
  suspicious at exactly that request.
- **A log on the device**, in plain words, not deletable from within the app.
- **No cloud backup** (`allowBackup="false"`) — the helper does not travel along
  to another device.

The list of requirements did not have to be invented; it has been published for
years by the [data protection authority of Baden-Württemberg][lfdi] and in the
German BSI module OPS.1.2.5.

[pol]: https://www.polizei-beratung.de/aktuelles/detailansicht/tech-support-scams-falsche-microsoft-mitarbeiter-am-telefon/
[wli]: https://www.watchlist-internet.at/news/achtung-bei-anrufen-von-microsoft/
[ic3]: https://www.ic3.gov/AnnualReport/Reports/2025_IC3Report.pdf
[lfdi]: https://www.baden-wuerttemberg.datenschutz.de/fernwartung/

## Three permissions, no more

| Permission | What for | Without it |
|---|---|---|
| `CALL_PHONE` | dial the stored number directly | the number lands in the phone app, one tap missing |
| `READ_PHONE_STATE` | pass the right SIM on dual-SIM devices | Android asks on every call |
| `ACCESS_NETWORK_STATE` | whether there is a connection at all | – (normal, no dialog) |

Deliberately **absent**: `BIND_ACCESSIBILITY_SERVICE`, anything around
`MediaProjection`, `QUERY_ALL_PACKAGES`, `REQUEST_INSTALL_PACKAGES`, location,
contacts, camera, microphone.

Instead of `QUERY_ALL_PACKAGES` there are five package names under `<queries>` —
the four remote helpers and TeamViewer's add-on. Reading the full app list needs
special approval from Google and could not be justified here.

## Installing

There is no ready-made build to download yet; it will come with the first
release. Until then, build it yourself:

```
./gradlew assembleDebug
```

The APK then sits in `app/build/outputs/apk/debug/`.

You need an Android SDK with API 36 and a JDK 17 — **not** a newer one: Gradle
8.14 chokes on four-part version numbers such as `26.0.2.1`.

## Layout

```
MainActivity          the home screen: two buttons, the report
AssistentActivity     the setup that checks itself
  ├── Assistent       which steps are open             (pure, testable)
  ├── Systemseiten    opens the right system page
  ├── Fernhilfeart    the four programs and what they can do
  ├── Diagnose        gathers the device state
  ├── Regeln          turns that into findings         (pure, testable)
  ├── Karten          which SIM places the call
  ├── Helfer          the one stored helper
  └── Protokoll       what happened, and when
```

The split between `Diagnose` and `Regeln` is the heart of it: `Regeln` knows
nothing about Android and nothing about strings, only the data class `Zustand`.
That is why nearly-full storage, a device not restarted for two weeks or an
out-of-date Android can be produced in a test — on a phone each would take an
hour.

Source and documentation are German; this file is the translation.

## Licence

Apache 2.0, see [LICENSE](LICENSE).

## Changelog

### 0.1.0 – in progress

First version, tried on a real device (Motorola edge 50 neo, Android 16). The
button places calls, the status report checks thirteen things, setup walks
through nine steps. Open points are in [TODO.en.md](TODO.en.md).
