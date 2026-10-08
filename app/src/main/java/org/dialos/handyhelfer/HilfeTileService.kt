package org.dialos.handyhelfer

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * Die Kachel in den Schnelleinstellungen.
 *
 * **Der wichtigste der drei Wege**, auch wenn er am unscheinbarsten aussieht:
 * Das Widget auf dem Startbildschirm hilft nur, wenn man bis dorthin kommt.
 * Wer in einer fremden App feststeckt und nicht mehr weiss, wie er dort
 * herauskommt - der haeufigste Anlass fuer einen Hilferuf ueberhaupt -, erreicht
 * den Startbildschirm vielleicht gerade nicht. Von oben wischen geht fast
 * immer, und zwar ueber jeder laufenden App.
 */
class HilfeTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val kachel = qsTile ?: return
        // Aktiv, sobald ein Helfer feststeht - sonst fuehrt die Kachel in die
        // Einrichtung, und das soll man ihr ansehen.
        kachel.state = if (Helfer.eingerichtet(this)) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        kachel.label = getString(R.string.app_name)
        kachel.updateTile()
    }

    /**
     * Lint meldet die Intent-Fassung von `startActivityAndCollapse` als
     * veraltet und will sie gar nicht sehen. Sie bleibt trotzdem stehen: Die
     * Nachfolgerin mit PendingIntent gibt es erst ab API 34, die App laeuft ab
     * API 26. Ohne den alten Zweig bliebe auf Android 8 bis 13 die Schublade
     * der Schnelleinstellungen offen ueber der App stehen.
     */
    @android.annotation.SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        val oeffnen = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // Ab Android 14 verlangt das System den Weg ueber einen PendingIntent;
        // startActivityAndCollapse(Intent) wirft dort eine UnsupportedOperation.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(
                PendingIntent.getActivity(
                    this, 0, oeffnen,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(oeffnen)
        }
    }
}
