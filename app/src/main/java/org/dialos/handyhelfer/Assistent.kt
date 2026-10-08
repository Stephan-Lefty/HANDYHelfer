package org.dialos.handyhelfer

/**
 * Was bei der Ersteinrichtung schon sitzt und was noch fehlt.
 *
 * Ohne Android-Bezug, damit die Faelle in `AssistentTest` ohne Geraet laufen.
 */
data class Stand(
    val fernhilfeInstalliert: Boolean,
    val bedienhilfeAktiv: Boolean,
    /** Kann der Helfer mit dieser Fernhilfe wirklich tippen, oder nur zusehen? */
    val fernhilfeKannSteuern: Boolean,
    /** Braucht sie ein Zusatzpaket dafuer, und fehlt es noch? */
    val zusatzNoetig: Boolean,
    val zusatzDa: Boolean,
    /** Verlangt sie ein Passwort, das sonst bei jeder Sitzung vorgelesen wird? */
    val fernhilfeBrauchtPasswort: Boolean,
    val helferDa: Boolean,
    val anrufErlaubt: Boolean,
    val mehrereKarten: Boolean,
    val karteFestgelegt: Boolean,
    val balkenDa: Boolean,
    val symbolDa: Boolean,
)

enum class SchrittArt {
    FERNHILFE_INSTALLIEREN,
    ZUSATZ_INSTALLIEREN,
    NUR_ZUSEHEN,
    KEINE_APP_PAUSE,
    BEDIENHILFE,
    FESTES_PASSWORT,
    KARTE_FUER_ANRUFE,
    HELFER,
    ANRUFEN_DUERFEN,
    BALKEN,
    SYMBOL,
}

/**
 * Ein Schritt der Einrichtung.
 *
 * [pruefbar] ist der ehrliche Teil: Manches kann die App nachsehen, anderes
 * nicht. Ein Haken, der nur behauptet, erledigt zu sein, waere schlimmer als
 * gar keiner - deshalb tragen unpruefbare Schritte kein Zeichen, sondern
 * einen Hinweis.
 *
 * [kuer] markiert, was die Einrichtung nicht blockiert.
 */
data class Schritt(
    val art: SchrittArt,
    val erledigt: Boolean,
    val pruefbar: Boolean = true,
    val kuer: Boolean = false,
)

/**
 * Die Liste, die sich selbst prueft.
 *
 * Der Unterschied zu einer Anleitung zum Abhaken: Wer hier einen Haken sieht,
 * hat ihn nicht selbst gesetzt. Dieselbe Liste laesst sich spaeter wieder
 * aufrufen, wenn etwas nicht mehr geht - dann steht dort, was sich geaendert
 * hat.
 */
object Assistent {

    fun schritte(stand: Stand): List<Schritt> {
        val liste = mutableListOf<Schritt>()

        liste += Schritt(SchrittArt.FERNHILFE_INSTALLIEREN, stand.fernhilfeInstalliert)

        if (stand.fernhilfeInstalliert) {
            // Das Zusatzpaket steht vor der Bedienhilfe: Ohne es gibt es den
            // Schalter gar nicht, den der naechste Schritt umlegen will.
            if (stand.zusatzNoetig && !stand.zusatzDa) {
                liste += Schritt(SchrittArt.ZUSATZ_INSTALLIEREN, erledigt = false)
            }

            if (stand.fernhilfeKannSteuern) {
                liste += Schritt(SchrittArt.BEDIENHILFE, stand.bedienhilfeAktiv)
            } else {
                // Ehrlich sagen, was nicht geht, statt einen Schalter
                // anzubieten, den Android ausgegraut laesst. Am 2026-10-08
                // am Geraet durchexerziert.
                liste += Schritt(SchrittArt.NUR_ZUSEHEN, erledigt = false, pruefbar = false)
            }

            if (stand.fernhilfeBrauchtPasswort) {
                liste += Schritt(SchrittArt.FESTES_PASSWORT, erledigt = false, pruefbar = false)
            }

            liste += Schritt(SchrittArt.KEINE_APP_PAUSE, erledigt = false, pruefbar = false)
        }

        // Nur wenn mehr als eine Karte steckt und keine als Standard gilt:
        // Sonst fragt Android bei jedem Anruf zurueck, mit welcher gewaehlt
        // werden soll - und der grosse Knopf fuehrt in einen Auswahldialog
        // statt zum Klingeln. Am 2026-10-08 am Geraet gesehen.
        if (stand.mehrereKarten && !stand.karteFestgelegt) {
            liste += Schritt(SchrittArt.KARTE_FUER_ANRUFE, erledigt = false)
        }

        liste += Schritt(SchrittArt.HELFER, stand.helferDa)
        liste += Schritt(SchrittArt.ANRUFEN_DUERFEN, stand.anrufErlaubt)
        liste += Schritt(SchrittArt.BALKEN, stand.balkenDa)
        liste += Schritt(SchrittArt.SYMBOL, stand.symbolDa, kuer = true)

        return liste
    }

    /**
     * Ob die Einrichtung durch ist.
     *
     * Die Kuer zaehlt nicht mit, und Unpruefbares auch nicht - sonst stuende
     * "fertig" nie da, obwohl alles sitzt.
     */
    fun fertig(stand: Stand): Boolean =
        schritte(stand).none { !it.erledigt && it.pruefbar && !it.kuer }

    /**
     * Was ohne diesen Schritt nicht geht - fuer die Zeile unter dem Titel.
     *
     * Bewusst nicht "wichtig/unwichtig", sondern die Folge: Wer liest, dass
     * der Helfer sonst nur zusehen kann, versteht, warum er die drei Dialoge
     * durchklicken soll.
     */
    fun blockiertHauptzweck(art: SchrittArt): Boolean = when (art) {
        SchrittArt.HELFER, SchrittArt.ANRUFEN_DUERFEN, SchrittArt.KARTE_FUER_ANRUFE -> true
        else -> false
    }
}
