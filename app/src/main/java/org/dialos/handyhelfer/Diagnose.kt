package org.dialos.handyhelfer

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings

/**
 * Traegt den Zustand des Geraets zusammen.
 *
 * **Was hier bewusst fehlt, ist so wichtig wie das, was drinsteht.** Kein
 * Bildschirminhalt, keine Kontakte, keine Standorte, keine Liste der
 * installierten Apps. Abgefragt wird ausschliesslich, was ohne eine einzige
 * gefaehrliche Berechtigung zu haben ist - der Rest waere ein Datenschatz, der
 * zu einem Werkzeug fuer Angehoerigenhilfe nicht passt.
 *
 * Aus demselben Grund gibt es keinen Server: Der Bericht wird am Geraet
 * angezeigt, und der Helfer liest ihn, sobald er den Bildschirm sieht. Er wird
 * nirgendwohin uebertragen.
 */
object Diagnose {

    fun erheben(context: Context): Zustand {
        val akku = akkuStand(context)
        val speicher = speicher()
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        val klingelmodus = audio.ringerMode
        return Zustand(
            hersteller = Build.MANUFACTURER,
            modell = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            sdk = Build.VERSION.SDK_INT,
            akkuProzent = akku.first,
            akkuLaedt = akku.second,
            akkusparmodus = power.isPowerSaveMode,
            speicherFreiMb = speicher.first,
            speicherGesamtMb = speicher.second,
            klingelLautstaerke = audio.getStreamVolume(AudioManager.STREAM_RING),
            klingelMaximum = audio.getStreamMaxVolume(AudioManager.STREAM_RING),
            stummOderVibration = klingelmodus == AudioManager.RINGER_MODE_SILENT ||
                klingelmodus == AudioManager.RINGER_MODE_VIBRATE,
            nichtStoeren = nichtStoeren(context),
            flugmodus = Settings.Global.getInt(
                context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0,
            ) != 0,
            netzVerbunden = netzVerbunden(context),
            schriftSkalierung = context.resources.configuration.fontScale,
            betriebszeitStunden = SystemClock.elapsedRealtime() / 3_600_000L,
            fernhilfeInstalliert = Fernhilfe.installiert(context),
        )
    }

    /** Ladestand in Prozent und ob gerade geladen wird. */
    private fun akkuStand(context: Context): Pair<Int, Boolean> {
        val stand: Intent? = context.registerReceiver(
            null, IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        )
        val level = stand?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = stand?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = stand?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val prozent = if (level >= 0 && scale > 0) level * 100 / scale else -1
        val laedt = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        return prozent to laedt
    }

    /** Freier und gesamter Speicher des Datenbereichs, in MB. */
    private fun speicher(): Pair<Long, Long> {
        val statFs = StatFs(Environment.getDataDirectory().path)
        val mb = 1024L * 1024L
        return statFs.availableBytes / mb to statFs.totalBytes / mb
    }

    /**
     * Ob "Nicht stoeren" laeuft.
     *
     * Ohne Zugriff auf die Benachrichtigungsrichtlinie antwortet Android mit
     * `INTERRUPTION_FILTER_UNKNOWN`. Das wird als "nicht feststellbar"
     * behandelt und nicht als "aus" - eine Warnung, die auf einer Vermutung
     * beruht, ist schlimmer als keine.
     */
    private fun nichtStoeren(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return when (nm.currentInterruptionFilter) {
            NotificationManager.INTERRUPTION_FILTER_NONE,
            NotificationManager.INTERRUPTION_FILTER_ALARMS,
            NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            -> true

            else -> false
        }
    }

    private fun netzVerbunden(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netz = cm.activeNetwork ?: return false
        val faehig = cm.getNetworkCapabilities(netz) ?: return false
        return faehig.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Woher ein Paket kam.
     *
     * Entscheidet ueber die Sperre: Android laesst bei Apps, die per Browser
     * geladen wurden, den Schalter unter den Bedienungshilfen ausgegraut
     * ("Gesteuert durch eingeschraenkte Einstellung"). Sitzungsbasierte
     * Installationen - F-Droid, Play Store, Geraeteverwaltung - sind davon
     * nicht betroffen.
     *
     * Am 2026-10-08 am Geraet belegt: RustDesk kam ueber
     * `com.google.android.packageinstaller`, und "RustDesk Input" war genau
     * deshalb nicht anfassbar.
     */
    fun installiertVon(context: Context, paket: String): String? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager.getInstallSourceInfo(paket).installingPackageName
        } else {
            // Vor Android 11 gibt es nur diesen Weg. Er ist seit 30 als
            // ueberholt gemeldet, aber dort die einzige Auskunft.
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(paket)
        }
    } catch (_: PackageManager.NameNotFoundException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

    /** Die Kennung des Paketinstallierers - der Weg ueber den Browser. */
    const val BROWSER_INSTALLIERER = "com.google.android.packageinstaller"

    /**
     * F-Droid.
     *
     * Der Umweg ueber F-Droid hilft nur, wenn die **App** installiert - nicht,
     * wenn man die APK von f-droid.org im Browser herunterlaedt. Dann ist der
     * Installierer wieder der Paketinstallierer und die Sperre dieselbe.
     * Deshalb fragt der Assistent zuerst nach F-Droid selbst.
     */
    const val FDROID_PAKET = "org.fdroid.fdroid"

    /**
     * Ob ein Paket installiert ist.
     *
     * Ab Android 11 nur sichtbar, wenn es im Manifest unter `<queries>` steht -
     * deshalb genau ein Paketname dort und kein `QUERY_ALL_PACKAGES`. Die
     * Berechtigung, die ganze App-Liste zu lesen, braucht bei Google eine
     * Sondergenehmigung und waere fuer diesen Zweck auch nicht zu rechtfertigen.
     */
    fun installiert(context: Context, paket: String): Boolean = try {
        context.packageManager.getPackageInfo(paket, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }
}
