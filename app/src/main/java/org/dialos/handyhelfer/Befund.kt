package org.dialos.handyhelfer

/**
 * Der Zustand eines Geraets, wie ihn [Diagnose] zusammentraegt.
 *
 * Bewusst eine reine Datenklasse ohne Android-Bezug: Die Regeln in [pruefen]
 * sind damit ohne Geraet und ohne Robolectric pruefbar. In DialOS Mobil liegt
 * die Haelfte der Logik hinter einem `android.Context` und ist deshalb nur am
 * Telefon zu testen - das sollte sich hier nicht wiederholen.
 */
data class Zustand(
    val hersteller: String,
    val modell: String,
    val androidVersion: String,
    val sdk: Int,
    val akkuProzent: Int,
    val akkuLaedt: Boolean,
    val akkusparmodus: Boolean,
    val speicherFreiMb: Long,
    val speicherGesamtMb: Long,
    val klingelLautstaerke: Int,
    val klingelMaximum: Int,
    val stummOderVibration: Boolean,
    val nichtStoeren: Boolean,
    val flugmodus: Boolean,
    val netzVerbunden: Boolean,
    val schriftSkalierung: Float,
    val betriebszeitStunden: Long,
    val fernhilfeInstalliert: Boolean,
)

/**
 * Hersteller und Modell zu einer Zeile, ohne Dopplung.
 *
 * Am 2026-10-08 auf dem Geraet gesehen: `Build.MANUFACTURER` ist "motorola",
 * `Build.MODEL` ist "motorola edge 50 neo" - zusammengesetzt stand dort
 * "Motorola motorola edge 50 neo". Andere Hersteller machen es anders
 * (Samsung meldet das Modell als "SM-A146P"), deshalb wird der Hersteller
 * nicht pauschal weggelassen, sondern nur, wenn das Modell ihn schon traegt.
 */
fun geraetName(hersteller: String, modell: String): String {
    val h = hersteller.trim()
    val m = modell.trim()
    if (h.isEmpty()) return m
    if (m.isEmpty()) return h
    val gross = h.replaceFirstChar { it.uppercase() }
    return if (m.startsWith(h, ignoreCase = true)) {
        // Die Schreibweise des Herstellerfeldes gewinnt, nicht die des Modells:
        // Sonst wird aus "OnePlus" + "oneplus 12" ein "Oneplus 12". Ein Test
        // hat genau das gefunden.
        gross + m.substring(h.length)
    } else {
        "$gross $m"
    }
}

enum class Stufe { HINWEIS, WARNUNG }

/**
 * Was dem Helfer auffallen soll. Der Text steht nicht hier, sondern als
 * Zeichenkette in den Ressourcen - sonst waere die Pruefung nicht uebersetzbar.
 * [werte] fuellt die Platzhalter darin, in der Reihenfolge der Vorlage.
 */
enum class Art {
    FLUGMODUS_AN,
    KEIN_NETZ,
    STUMM,
    KLINGELTON_LEISE,
    NICHT_STOEREN,
    SPEICHER_FAST_VOLL,
    SPEICHER_KNAPP,
    AKKU_SCHWACH,
    AKKUSPARMODUS_AN,
    LANGE_KEIN_NEUSTART,
    SCHRIFT_SEHR_GROSS,
    ANDROID_ALT,
    FERNHILFE_FEHLT,
}

data class Befund(val art: Art, val stufe: Stufe, val werte: List<String> = emptyList())

/**
 * Die Regeln, nach denen aus einem [Zustand] Befunde werden.
 *
 * Alle Schwellen stehen hier als Konstante mit Begruendung. Wer eine aendert,
 * aendert eine Zahl und nicht ein verstreutes `if`.
 */
object Regeln {

    /** Unter 1 GB meldet Android selbst nichts mehr, Updates scheitern aber schon. */
    const val SPEICHER_FAST_VOLL_MB = 1_024L

    /** Darunter wird es eng, aber noch nicht akut. */
    const val SPEICHER_KNAPP_MB = 3_072L

    /** Zusaetzlich relativ: Ein 32-GB-Geraet mit 3 GB frei ist nicht knapp. */
    const val SPEICHER_KNAPP_ANTEIL = 0.10

    const val AKKU_SCHWACH_PROZENT = 15

    /** Unter einem Viertel ueberhoert man das Klingeln im Nebenzimmer. */
    const val KLINGELTON_LEISE_ANTEIL = 0.25

    /**
     * Zwei Wochen ohne Neustart. Keine feste Grenze von Android, sondern
     * Erfahrung: Ein Grossteil der "das Handy spinnt"-Faelle loest sich mit
     * einem Neustart, und danach fragt man gar nicht erst weiter.
     */
    const val LANGE_KEIN_NEUSTART_STUNDEN = 14L * 24L

    /** Ab hier bricht das Layout vieler Apps um, was wie ein Fehler aussieht. */
    const val SCHRIFT_SEHR_GROSS = 1.3f

    /** Android 9 und aelter bekommt keine Sicherheitsaktualisierungen mehr. */
    const val ANDROID_ALT_SDK = 28

    /**
     * Prueft einen Zustand und gibt die Befunde zurueck - Warnungen zuerst,
     * innerhalb einer Stufe in der Reihenfolge, in der sie hier stehen.
     *
     * Die Reihenfolge ist nicht beliebig: Ganz oben steht, was erklaert, warum
     * jemand *nicht erreichbar* war. Das ist der haeufigste Anlass fuer einen
     * Anruf beim Sohn, und es ist der Befund, den die Betroffene selbst am
     * wenigsten vermutet.
     */
    fun pruefen(z: Zustand): List<Befund> {
        val befunde = mutableListOf<Befund>()

        if (z.flugmodus) befunde += Befund(Art.FLUGMODUS_AN, Stufe.WARNUNG)
        if (!z.netzVerbunden && !z.flugmodus) befunde += Befund(Art.KEIN_NETZ, Stufe.WARNUNG)
        if (z.stummOderVibration) befunde += Befund(Art.STUMM, Stufe.WARNUNG)
        if (z.nichtStoeren) befunde += Befund(Art.NICHT_STOEREN, Stufe.WARNUNG)

        if (z.speicherFreiMb < SPEICHER_FAST_VOLL_MB) {
            befunde += Befund(Art.SPEICHER_FAST_VOLL, Stufe.WARNUNG, listOf(z.speicherFreiMb.toString()))
        }

        // Hinweise
        if (!z.stummOderVibration && z.klingelMaximum > 0 &&
            z.klingelLautstaerke < z.klingelMaximum * KLINGELTON_LEISE_ANTEIL
        ) {
            befunde += Befund(
                Art.KLINGELTON_LEISE, Stufe.HINWEIS,
                listOf(z.klingelLautstaerke.toString(), z.klingelMaximum.toString()),
            )
        }

        if (z.speicherFreiMb >= SPEICHER_FAST_VOLL_MB &&
            z.speicherFreiMb < SPEICHER_KNAPP_MB &&
            z.speicherGesamtMb > 0 &&
            z.speicherFreiMb.toDouble() / z.speicherGesamtMb < SPEICHER_KNAPP_ANTEIL
        ) {
            befunde += Befund(Art.SPEICHER_KNAPP, Stufe.HINWEIS, listOf(z.speicherFreiMb.toString()))
        }

        if (z.akkuProzent < AKKU_SCHWACH_PROZENT && !z.akkuLaedt) {
            befunde += Befund(Art.AKKU_SCHWACH, Stufe.HINWEIS, listOf(z.akkuProzent.toString()))
        }
        if (z.akkusparmodus) befunde += Befund(Art.AKKUSPARMODUS_AN, Stufe.HINWEIS)

        if (z.betriebszeitStunden >= LANGE_KEIN_NEUSTART_STUNDEN) {
            befunde += Befund(
                Art.LANGE_KEIN_NEUSTART, Stufe.HINWEIS,
                listOf((z.betriebszeitStunden / 24).toString()),
            )
        }

        if (z.schriftSkalierung >= SCHRIFT_SEHR_GROSS) {
            befunde += Befund(Art.SCHRIFT_SEHR_GROSS, Stufe.HINWEIS)
        }
        if (z.sdk <= ANDROID_ALT_SDK) {
            befunde += Befund(Art.ANDROID_ALT, Stufe.HINWEIS, listOf(z.androidVersion))
        }
        if (!z.fernhilfeInstalliert) befunde += Befund(Art.FERNHILFE_FEHLT, Stufe.HINWEIS)

        return befunde.sortedBy { if (it.stufe == Stufe.WARNUNG) 0 else 1 }
    }
}
