package org.dialos.handyhelfer

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

/**
 * Legt den Knopf auf den Startbildschirm - ohne dass jemand die Widget-Liste
 * durchsuchen muss.
 *
 * Das ist der Unterschied zwischen "es gibt ein Widget" und "der Knopf ist
 * da". Die Hürde liegt nicht im Widget selbst, sondern im Weg dorthin: lange
 * auf eine freie Stelle tippen, "Widgets" finden, in einer alphabetischen
 * Liste von sechzig Einträgen das richtige suchen, an die richtige Stelle
 * ziehen. Wer das kann, braucht HANDYHelfer nicht.
 *
 * Seit Android 8 (API 26, zugleich unser minSdk) darf eine App stattdessen
 * anbieten, das Widget zu platzieren: Es erscheint ein Systemdialog mit einer
 * Vorschau und einem Knopf. Mehr ist nicht zu tun.
 */
object Startbildschirm {

    private fun bestandteil(context: Context) =
        ComponentName(context, HilfeWidgetProvider::class.java)

    /** Ob der Knopf schon irgendwo auf einem Startbildschirm liegt. */
    fun knopfLiegtSchonDa(context: Context): Boolean =
        AppWidgetManager.getInstance(context)
            .getAppWidgetIds(bestandteil(context))
            .isNotEmpty()

    /**
     * Bietet an, den Knopf abzulegen.
     *
     * Gibt `false` zurueck, wenn der Startbildschirm das nicht unterstuetzt -
     * manche Hersteller-Oberflaechen und die meisten alternativen Launcher
     * koennen es nicht. Dann muss die Oberflaeche den Weg von Hand erklaeren,
     * statt stumm nichts zu tun.
     */
    fun knopfAblegenAnbieten(context: Context): Boolean {
        val manager = AppWidgetManager.getInstance(context)
        if (!manager.isRequestPinAppWidgetSupported) return false
        return manager.requestPinAppWidget(bestandteil(context), null, null)
    }
}
