package org.dialos.handyhelfer

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Haelt den Inhalt aus Statusleiste und Navigationsleiste heraus.
 *
 * Ab Android 15 zeichnet jede App mit targetSdk 35 oder hoeher zwingend unter
 * die Systemleisten ("edge to edge"); `android:statusBarColor` und
 * `fitsSystemWindows` allein reichen dafuer nicht mehr.
 *
 * Am 2026-10-08 auf einem Motorola edge 50 neo (Android 16) gesehen: Die erste
 * Textzeile der Einrichtung stand hinter der Uhrzeit. Im Emulator und in der
 * Vorschau des Layouteditors faellt das nicht auf - das hier ist der Grund,
 * warum eine App vor der Veroeffentlichung auf einem echten Geraet laufen muss.
 */
fun View.raenderBeachten() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { sicht, fenster ->
        val raender = fenster.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
        )
        sicht.updatePadding(
            left = raender.left,
            top = raender.top,
            right = raender.right,
            bottom = raender.bottom,
        )
        // Nicht verbrauchen: Kindansichten duerfen die Raender ebenfalls sehen.
        fenster
    }
}
