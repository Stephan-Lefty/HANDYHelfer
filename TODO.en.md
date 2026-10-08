[Deutsch](TODO.md) | [English](TODO.en.md)

# TODO – HANDYHelfer

> **As of 2026-10-08.** First day. 59 tests, lint clean, debug APK ~5.3 MB.
>
> **Tried on a real device** — Motorola edge 50 neo, Android 16, with TeamViewer
> QuickSupport. The button places calls without asking which SIM, the remote
> helper is detected, setup walks through nine steps, both widgets sit on the
> home screen, the log records everything.

## Urgent

### A real remote session has never been established

Testing stopped at opening the remote helper. Whether a helper can actually
connect, see the screen and tap — the whole point of the exercise — is untried.
That needs two devices and a second person.

### The accessibility service is the fragile part

It works, but nothing guarantees it keeps working. Worth measuring over time:
does the service survive a reboot, does Android's battery optimisation shut it
down, and does Motorola's own power saving interfere? For DialOS Mobil a Samsung
Galaxy A14 killed a foreground service 1.6 times a day — the same thing could
happen here, only less visibly.

## Open

### Things the app cannot check

Three steps carry a question mark instead of a tick, and that is deliberate —
but it means they stay open forever:

- **A permanent password** in the remote helper. Its settings are not readable
  by other apps.
- **App pausing** for the remote helper and its add-on.
  `isAutoRevokeWhitelisted` does not answer for foreign packages; measured on
  2026-10-08, the switch was visibly on and the query reported nothing.

Whether `GET_APP_OPS_STATS` would help is untested — it is a signature
permission, so probably not.

### "Do not disturb" is often undetectable

`Diagnose.nichtStoeren` asks `NotificationManager.currentInterruptionFilter`.
Without access to the notification policy Android answers
`INTERRUPTION_FILTER_UNKNOWN`, which is deliberately treated as "cannot tell"
rather than "off" — a warning on suspicion would be worse than none.

The consequence: precisely the finding that explains one of the most common
cases ("mother does not pick up") is missing most often. Measure on a device
what the filter actually returns without the permission.

### The helper learns nothing until they pick up

Today the alert is a phone call, nothing more. That was a deliberate start: no
server, no Firebase, no account, and it works on any network.

What is missing shows when the son does not answer: he never learns someone
needed him. Sending a message afterwards would work without any permission via
`ACTION_SENDTO` with `smsto:` — the SMS app opens pre-filled, one tap missing.
Build it when the case actually arises.

### Before a release

- **Screenshots**: home screen and setup exist; log and help are missing. Watch
  out — on 2026-10-08 four files showed the **lock screen with a private photo**
  instead of the app, because the display had gone off between captures. They
  were deleted and never committed. Check every image before it goes into the
  repository.
- **Privacy statement** (`PRIVACY.md` + `PRIVACY.en.md`). It will be short: the
  app collects nothing, transmits nothing and has no network connection.
- **Signing key** (`handyhelfer-release.jks`); instructions are at the top of
  `app/build.gradle.kts`.
- **Austrian example number**: `RufnummerTest` still uses an invented
  `+43 664 …`. The German examples now use official "drama numbers" from the
  Bundesnetzagentur; find the Austrian equivalent.

## Done

- **2026-10-08, settled at the device** — the foundation assumption was wrong.
  RustDesk cannot control a phone remotely: Android grants the accessibility
  service only to apps from Google Play, RustDesk withdrew itself from there,
  and on the test device the menu entry to lift the restriction is missing
  entirely. Installing via F-Droid changes nothing — proven, not assumed.
  HANDYHelfer now knows four remote helpers and, for each, whether it is open
  source and whether it can really control. The suggestion is TeamViewer
  QuickSupport, with its Universal Add-On as a step of its own.

- **2026-10-08, five bugs only a device could show** — content running under the
  status bar (edge-to-edge is mandatory from Android 15); no way back from
  sub-screens because the theme was `NoActionBar`; "Motorola motorola edge 50
  neo" in the device line; the call stalling in `SELECT_PHONE_ACCOUNT` because
  two SIMs were present and neither was default; and the accessibility service
  living in TeamViewer's **add-on** rather than its main package.

- **2026-10-08** — repository created. Scaffolding after the pattern of DialOS
  Mobil (Kotlin, compileSdk 36, minSdk 26, JDK 17). Icon recoloured onto the
  shared palette, plus a version without lettering for the launcher and a banner
  after the POSTKutsche pattern. Button, helper setup, status report with
  thirteen rules, log, help page — in German and English.

## Two things not worth rediscovering

**Gradle does not run with the default JDK.** The system has OpenJDK 26 and
Gradle 8.14 chokes on its four-part version number
(`IllegalArgumentException: 26.0.2.1`). Before building:

```
export JAVA_HOME=~/.gradle/jdks/eclipse_adoptium-17-amd64-linux.2
```

**Lint's version suggestions are wrong here.** It flags all four libraries as
outdated, but `core-ktx 1.19.1` demands compileSdk 37 while AGP 8.13 recommends
at most 36. Equally wrong: renaming `mipmap-anydpi-v26` to `mipmap-anydpi` —
after that AAPT cannot find the icon. Both are exempted in `app/lint.xml`, with
dates.
