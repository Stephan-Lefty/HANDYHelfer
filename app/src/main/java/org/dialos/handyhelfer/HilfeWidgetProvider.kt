package org.dialos.handyhelfer

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * Der bildschirmbreite Balken fuer den Startbildschirm.
 *
 * Warum so gross: Dieselbe Ueberlegung wie beim Widget von DialOS Mobil - ein
 * App-Symbol zwischen zwanzig anderen ist fuer jemanden, der mit dem Handy
 * fremdelt, kein brauchbares Ziel. Ein Balken ueber die volle Breite schon.
 *
 * **Er ruft nicht sofort an, sondern oeffnet die App.** Das ist ein Tipp mehr
 * und trotzdem richtig: Auf der Startseite steht der Zustandsbericht, den der
 * Helfer gleich mitlesen soll, und ein Widget laesst sich beim Aufraeumen des
 * Startbildschirms leicht streifen. Ein versehentlicher Anruf beim Sohn ist
 * kein Drama - aber wer ihn zweimal ausloest, traut dem Knopf nicht mehr.
 */
class HilfeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, bauen(context))
        }
    }

    companion object {

        /**
         * Zeichnet alle vorhandenen Widgets neu.
         *
         * Wird nach einem Helferwechsel gerufen: Sonst stuende im Balken noch
         * der alte Name, und eine Anzeige, die etwas anderes behauptet als der
         * Zustand, ist schlimmer als gar keine.
         */
        fun erneuern(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                android.content.ComponentName(context, HilfeWidgetProvider::class.java),
            )
            ids.forEach { manager.updateAppWidget(it, bauen(context)) }
        }

        private fun bauen(context: Context): RemoteViews {
            val sicht = RemoteViews(context.packageName, R.layout.widget_hilfe)

            // Der Name steht im Knopf, nicht darunter: "Hilfe von Stephan"
            // sagt auf einen Blick, wer kommt - und liest sich nicht wie ein
            // Notruf. Wer den Balken sieht, soll keine Hemmung haben, ihn zu
            // druecken.
            val helfer = Helfer.lesen(context)
            sicht.setTextViewText(
                R.id.widget_knopf,
                if (helfer == null) {
                    context.getString(R.string.widget_niemand_festgelegt)
                } else {
                    context.getString(R.string.hilfe_von, helfer.name)
                },
            )

            val oeffnen = Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            sicht.setOnClickPendingIntent(
                R.id.widget_knopf,
                PendingIntent.getActivity(
                    context, 0, oeffnen,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            return sicht
        }
    }
}
