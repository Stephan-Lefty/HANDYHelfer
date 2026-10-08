package org.dialos.handyhelfer

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * Das quadratische Widget - eine Zelle breit, so hoch wie der Balken daneben.
 *
 * Warum zwei Widgets statt eines verstellbaren: Sie sagen Verschiedenes. Der
 * [HilfeWidgetProvider] traegt den Namen des Helfers ("Hilfe von Stephan") und
 * ist die Handlung. Dieses hier traegt den Namen des Programms und ist der
 * Wiedererkennungswert - fuer den Platz neben dem Balken, oder fuer einen
 * Startbildschirm, auf dem die volle Breite schon vergeben ist.
 *
 * Beides in ein Widget zu packen, das sich je nach Groesse anders verhaelt,
 * waere technisch moeglich und praktisch eine Falle: Wer es schmaler zieht,
 * verlöre den Namen des Helfers, ohne das zu beabsichtigen.
 */
class SymbolWidgetProvider : AppWidgetProvider() {

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

        private fun bestandteil(context: Context) =
            ComponentName(context, SymbolWidgetProvider::class.java)

        fun liegtSchonDa(context: Context): Boolean =
            AppWidgetManager.getInstance(context)
                .getAppWidgetIds(bestandteil(context))
                .isNotEmpty()

        fun ablegenAnbieten(context: Context): Boolean {
            val manager = AppWidgetManager.getInstance(context)
            if (!manager.isRequestPinAppWidgetSupported) return false
            return manager.requestPinAppWidget(bestandteil(context), null, null)
        }

        private fun bauen(context: Context): RemoteViews {
            val sicht = RemoteViews(context.packageName, R.layout.widget_symbol)
            val oeffnen = Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            sicht.setOnClickPendingIntent(
                R.id.symbol_bild,
                PendingIntent.getActivity(
                    context, 0, oeffnen,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            return sicht
        }
    }
}
