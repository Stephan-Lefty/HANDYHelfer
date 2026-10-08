package org.dialos.handyhelfer

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/**
 * Die Bruecke zu RustDesk.
 *
 * **HANDYHelfer steuert nichts fern und sieht keinen Bildschirm.** Die
 * eigentliche Fernwartung macht RustDesk - quelloffen, mit eigenem Relay
 * betreibbar, und seit Jahren gepflegt. Das hier nachzubauen waere Monate
 * Arbeit fuer ein schlechteres Ergebnis, und es waere ausserdem aussichtslos:
 * Seit Android 14 verlangt `MediaProjection` fuer *jede* Sitzung eine neue
 * Zustimmung am Geraet, und seit Android 15 QPR1 bricht die Aufnahme beim
 * Sperren des Bildschirms ab. Daran scheitert auch TeamViewer.
 *
 * Was HANDYHelfer beitraegt, ist der Weg dorthin: einen Knopf statt einer
 * Kette aus Anruf, Kennung vorlesen, Passwort vorlesen und drei Dialogen.
 */
object Fernhilfe {

    fun installiert(context: Context): Boolean =
        Diagnose.installiert(context, Diagnose.FERNHILFE_PAKET)

    /**
     * Oeffnet RustDesk. Gibt `false` zurueck, wenn es nicht installiert ist
     * oder keinen Startpunkt anbietet - dann soll die Oberflaeche erklaeren
     * statt stumm nichts zu tun.
     *
     * Ob sich RustDesk darueber hinaus von aussen in den Empfangsmodus bringen
     * laesst, ist offen und steht in TODO.md. Bis das geklaert ist, bleibt es
     * beim Oeffnen - die Schalter setzt die Betroffene selbst.
     */
    fun oeffnen(context: Context): Boolean {
        val start = context.packageManager
            .getLaunchIntentForPackage(Diagnose.FERNHILFE_PAKET) ?: return false
        start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(start)
        return true
    }

    /**
     * Bezugsquelle fuer RustDesk.
     *
     * **Nicht der Play Store** - dort gibt es RustDesk nicht mehr. Das Projekt
     * hat die Android-App freiwillig zurueckgezogen, als Reaktion auf
     * Missbrauch durch Betrueger. Ein `market://`-Verweis liefe also ins Leere.
     *
     * Das ist zugleich die beste Begruendung fuer HANDYHelfer, die sich finden
     * laesst: Das Werkzeug selbst ist nicht das Problem - der Weg, auf dem ein
     * Fremder jemanden zur Verbindung ueberredet, ist es. Genau den macht ein
     * fest hinterlegter Helfer zu.
     */
    const val BEZUGSQUELLE = "https://rustdesk.com/download"

    fun installierenAnbieten(context: Context) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, BEZUGSQUELLE.toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
