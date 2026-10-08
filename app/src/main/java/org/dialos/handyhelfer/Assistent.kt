package org.dialos.handyhelfer

/**
 * Was bei der Ersteinrichtung schon sitzt und was noch fehlt.
 *
 * Ohne Android-Bezug, damit die Faelle in `AssistentTest` ohne Geraet laufen.
 */
data class Stand(
    val fernhilfeInstalliert: Boolean,
    val bedienhilfeAktiv: Boolean,
    val fernhilfeAusBrowser: Boolean,
    val helferDa: Boolean,
    val anrufErlaubt: Boolean,
    val mehrereKarten: Boolean,
    val karteFestgelegt: Boolean,
    val balkenDa: Boolean,
    val symbolDa: Boolean,
)

enum class SchrittArt {
    FERNHILFE_INSTALLIEREN,
    EINGESCHRAENKTE_EINSTELLUNGEN,
    NEU_INSTALLIEREN_AUS_FDROID,
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
            // Nur zeigen, solange die Bedienhilfe noch nicht laeuft: Danach
            // ist die Frage beantwortet, und ein Schritt, der sich nicht
            // pruefen laesst, soll nicht dauerhaft herumstehen.
            //
            // Er steht VOR der Bedienhilfe, weil er sie sperrt: Bei einer per
            // Browser geladenen APK ist der Schalter ausgegraut, und wer das
            // nicht weiss, sucht an der falschen Stelle.
            if (!stand.bedienhilfeAktiv) {
                // Kam die App aus dem Browser, ist der Schalter gesperrt und
                // laesst sich auf vielen Geraeten gar nicht freigeben - in der
                // App-Info fehlt dort das Menue dafuer. Der ehrliche Rat ist
                // dann nicht "such den Schalter", sondern "installier sie neu
                // aus F-Droid": Das installiert sitzungsbasiert, und die
                // Sperre greift gar nicht erst.
                if (stand.fernhilfeAusBrowser) {
                    liste += Schritt(SchrittArt.NEU_INSTALLIEREN_AUS_FDROID, erledigt = false)
                } else {
                    liste += Schritt(
                        SchrittArt.EINGESCHRAENKTE_EINSTELLUNGEN,
                        erledigt = false,
                        pruefbar = false,
                    )
                }
            }
            liste += Schritt(SchrittArt.BEDIENHILFE, stand.bedienhilfeAktiv)

            // Nicht pruefbar: RustDesks Einstellungen sind fuer fremde Apps
            // nicht lesbar. Der Schritt steht trotzdem da, weil er den Alltag
            // entscheidet - ohne festes Passwort muss bei jeder Sitzung ein
            // Einmalpasswort vorgelesen werden, und genau daran scheitert es.
            liste += Schritt(
                SchrittArt.FESTES_PASSWORT,
                erledigt = false,
                pruefbar = false,
            )

            // Eine Fernwartungs-App liegt monatelang ungenutzt herum - genau
            // der Fall, fuer den Android die Rechte wieder einzieht. Im
            // Ernstfall stuende dann ein RustDesk ohne Berechtigungen da.
            //
            // Unpruefbar: `isAutoRevokeWhitelisted` beantwortet die Frage fuer
            // *fremde* Pakete nicht. Am 2026-10-08 ausprobiert - am Geraet
            // stand der Schalter sichtbar auf an, die Abfrage meldete trotzdem
            // nichts. Lieber ein Fragezeichen als ein verschwiegenes Problem.
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
