package org.dialos.handyhelfer

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

/**
 * Oeffnet die Systemseiten, auf denen die Einrichtung stattfindet.
 *
 * Das ist der Kern des Assistenten: Nicht beschreiben, wo ein Schalter liegt,
 * sondern die Seite aufmachen, auf der er steht. Ein Menuepfad aus dem
 * Gedaechtnis stimmt bei Samsung, Xiaomi und Motorola jeweils anders - ein
 * Intent trifft ueberall.
 *
 * Jede Funktion gibt zurueck, ob es geklappt hat. Nicht jedes Geraet kennt
 * jede Seite; dann soll die Oberflaeche den Weg erklaeren, statt dass nichts
 * passiert.
 */
object Systemseiten {

    /** Die Bedienungshilfen - dort steht der Schalter fuer die Eingabesteuerung. */
    fun bedienungshilfen(context: Context): Boolean =
        starten(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

    /**
     * Die App-Info von RustDesk.
     *
     * Hier liegt bei einer per Browser geladenen APK die Freigabe der
     * eingeschraenkten Einstellungen - ohne sie bleibt der Schalter unter den
     * Bedienungshilfen ausgegraut. Den genauen Weg von dort aus (bei Android 16
     * ueber das Menue oben rechts) nennt der Assistent bewusst nicht
     * wortgetreu: Er heisst je nach Hersteller anders.
     */
    fun appInfoFernhilfe(context: Context): Boolean = starten(
        context,
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            "package:${Diagnose.FERNHILFE_PAKET}".toUri(),
        ),
    )

    /**
     * Die F-Droid-Seite von RustDesk.
     *
     * Der empfohlene Weg, wenn die Browser-Installation die Eingabesteuerung
     * sperrt: F-Droid installiert sitzungsbasiert, und die eingeschraenkten
     * Einstellungen greifen dann nicht.
     */
    fun fdroidSeite(context: Context): Boolean = starten(
        context,
        Intent(
            Intent.ACTION_VIEW,
            "https://f-droid.org/packages/${Diagnose.FERNHILFE_PAKET}/".toUri(),
        ),
    )

    /** F-Droid selbst - die App, nicht der Katalogeintrag. */
    fun fdroidHolen(context: Context): Boolean =
        starten(context, Intent(Intent.ACTION_VIEW, "https://f-droid.org/".toUri()))

    /** Die Bezugsquelle fuer RustDesk - nicht der Play Store, dort fehlt es. */
    fun fernhilfeHolen(context: Context): Boolean =
        starten(context, Intent(Intent.ACTION_VIEW, Fernhilfe.BEZUGSQUELLE.toUri()))

    /**
     * Ob der Bedienhilfe-Dienst von RustDesk laeuft.
     *
     * Gelesen aus `enabled_accessibility_services` - das ist dieselbe Quelle,
     * aus der auch `adb shell settings get secure` antwortet, und sie ist ohne
     * Berechtigung lesbar.
     */
    fun bedienhilfeAktiv(context: Context): Boolean {
        val aktive = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ).orEmpty()
        return aktive.contains(Diagnose.FERNHILFE_PAKET, ignoreCase = true)
    }

    private fun starten(context: Context, absicht: Intent): Boolean = try {
        context.startActivity(absicht.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: android.content.ActivityNotFoundException) {
        false
    }
}
