package org.dialos.handyhelfer

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/**
 * Die Bruecke zur Fernhilfe - zu der, die da ist.
 *
 * **HANDYHelfer steuert nichts fern und sieht keinen Bildschirm.** Das macht
 * ein eigenes Programm. Das hier nachzubauen waere aussichtslos: Seit
 * Android 14 verlangt `MediaProjection` fuer jede Sitzung eine neue
 * Zustimmung, seit Android 15 bricht die Aufnahme beim Sperren des
 * Bildschirms ab, und der Bedienhilfe-Dienst steht nur Apps aus dem Play
 * Store offen. Daran scheitert auch TeamViewer.
 *
 * Was HANDYHelfer beitraegt, ist der Weg dorthin: ein Knopf statt einer Kette
 * aus Anruf, Kennung vorlesen, Passwort vorlesen und drei Dialogen.
 */
object Fernhilfe {

    /** Welche der bekannten Fernhilfen installiert ist, oder `null`. */
    fun gefunden(context: Context): Fernhilfeart? =
        Fernhilfeart.entries.firstOrNull { Diagnose.installiert(context, it.paket) }

    fun installiert(context: Context): Boolean = gefunden(context) != null

    /**
     * Oeffnet die gefundene Fernhilfe. Gibt `false` zurueck, wenn keine da ist
     * oder sie keinen Startpunkt anbietet - dann soll die Oberflaeche
     * erklaeren statt stumm nichts zu tun.
     */
    fun oeffnen(context: Context): Boolean {
        val art = gefunden(context) ?: return false
        val start = context.packageManager.getLaunchIntentForPackage(art.paket) ?: return false
        start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(start)
        return true
    }

    /** Fuehrt zur Bezugsquelle - der vorgeschlagenen, wenn noch nichts da ist. */
    fun installierenAnbieten(context: Context) {
        val quelle = (gefunden(context) ?: Fernhilfeart.VORSCHLAG).bezugsquelle
        context.startActivity(
            Intent(Intent.ACTION_VIEW, quelle.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
