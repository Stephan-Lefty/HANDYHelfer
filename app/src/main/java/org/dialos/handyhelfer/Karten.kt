package org.dialos.handyhelfer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import androidx.core.content.edit

/**
 * Welche SIM-Karte den Anruf macht.
 *
 * **Am 2026-10-08 am Geraet gefunden**, und ohne diesen Test waere es nie
 * aufgefallen: Der Anruf lief korrekt los, blieb aber im Zustand
 * `SELECT_PHONE_ACCOUNT` stehen - das Telefon hat zwei Karten und keine davon
 * gilt als Standard, also fragte Android zurueck, mit welcher gewaehlt werden
 * soll.
 *
 * Fuer die Zielgruppe ist das der Tod des Knopfes: Sie drueckt "Hilfe von
 * Stephan", und statt dass es klingelt, erscheint ein Dialog mit zwei Zeilen,
 * die beide nach Kauderwelsch aussehen. Genau diesen Moment soll die App
 * abschaffen.
 *
 * Deshalb merkt sich HANDYHelfer die Karte und gibt sie beim Anruf mit. Wo nur
 * eine Karte steckt, aendert sich nichts.
 */
object Karten {

    private const val DATEI = "handyhelfer"
    private const val KARTE = "karte_fuer_anrufe"

    /**
     * Ob die Kartenliste ueberhaupt lesbar ist.
     *
     * `getCallCapablePhoneAccounts` verlangt READ_PHONE_STATE. Die Berechtigung
     * ist freiwillig: Ohne sie laeuft alles wie zuvor - auf einem Geraet mit
     * einer Karte voellig unauffaellig, auf einem mit zweien mit dem
     * Rueckfragedialog.
     */
    fun darfLesen(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED

    private fun telecom(context: Context) =
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

    /** Alle Karten, mit denen sich telefonieren laesst. */
    fun verfuegbare(context: Context): List<PhoneAccountHandle> {
        if (!darfLesen(context)) return emptyList()
        return try {
            telecom(context).callCapablePhoneAccounts
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    fun mehrereKarten(context: Context): Boolean = verfuegbare(context).size > 1

    /**
     * Ob feststeht, mit welcher Karte gewaehlt wird - entweder weil das System
     * eine Standardkarte kennt, oder weil hier eine hinterlegt ist.
     */
    fun festgelegt(context: Context): Boolean {
        if (gespeicherte(context) != null) return true
        return try {
            telecom(context).getDefaultOutgoingPhoneAccount("tel") != null
        } catch (_: SecurityException) {
            false
        }
    }

    fun gespeicherte(context: Context): PhoneAccountHandle? {
        val abgelegt = context.getSharedPreferences(DATEI, Context.MODE_PRIVATE)
            .getString(KARTE, null) ?: return null
        // Nur zurueckgeben, wenn die Karte noch steckt: Nach einem Kartentausch
        // zeigte der Verweis sonst ins Leere und der Anruf schluege fehl.
        return verfuegbare(context).firstOrNull { it.id == abgelegt }
    }

    /**
     * Der Name, den das System der Karte gibt - "1&1", "YELLLOW", "eSIM".
     *
     * **Am 2026-10-08 am Geraet gefunden:** Das Label der Telefonkonten ist
     * leer, und die Auswahl zeigte daraufhin "4" und "3" - die blanken
     * Kennungen. Genau das soll sie nicht: Wer zwischen zwei Zeilen
     * Kauderwelsch waehlen soll, waehlt gar nicht.
     *
     * Die lesbaren Namen stehen in den Vertragsdaten, nicht in den
     * Telefonkonten. Bei Telephony-Konten ist `handle.id` die Vertragskennung,
     * darueber findet sich der Anzeigename.
     */
    fun name(context: Context, karte: PhoneAccountHandle): String {
        val label = try {
            telecom(context).getPhoneAccount(karte)?.label?.toString()
        } catch (_: SecurityException) {
            null
        }

        val vertrag = vertragZu(context, karte)
        return besterName(
            label = label,
            anzeigename = vertrag?.displayName?.toString(),
            anbieter = vertrag?.carrierName?.toString(),
            platz = vertrag?.simSlotIndex ?: -1,
            kennung = karte.id,
        )
    }

    private fun vertragZu(context: Context, karte: PhoneAccountHandle): SubscriptionInfo? {
        if (!darfLesen(context)) return null
        val kennung = karte.id.toIntOrNull() ?: return null
        val verwaltung = context.getSystemService(SubscriptionManager::class.java) ?: return null
        return try {
            verwaltung.activeSubscriptionInfoList?.firstOrNull { it.subscriptionId == kennung }
        } catch (_: SecurityException) {
            null
        }
    }

    /**
     * Welcher Name genommen wird, und in welcher Reihenfolge.
     *
     * Ohne Android, damit die Faelle in `KartenNameTest` ohne Geraet laufen -
     * zwei SIM-Karten mit verschiedenen Anbietern sind sonst nur schwer
     * herzustellen.
     *
     * Der Anbietername kommt vor der Position, aber nach dem Anzeigenamen:
     * Auf dem Testgeraet meldete eine Karte `displayName=1&1` und
     * `carrierName=3` - der Anzeigename war der brauchbare, der Anbietername
     * eine blanke Ziffer. Reine Zahlen gelten deshalb als unbrauchbar.
     */
    fun besterName(
        label: String?,
        anzeigename: String?,
        anbieter: String?,
        platz: Int,
        kennung: String,
    ): String {
        for (kandidat in listOf(label, anzeigename, anbieter)) {
            val sauber = kandidat?.trim().orEmpty()
            if (sauber.isNotEmpty() && !sauber.all { it.isDigit() }) return sauber
        }
        // Kein Name zu holen: wenigstens der Steckplatz, menschlich gezaehlt.
        if (platz >= 0) return "SIM ${platz + 1}"
        return kennung
    }

    /** Der Name der hinterlegten Karte, oder `null`, wenn keine feststeht. */
    fun nameDerGespeicherten(context: Context): String? =
        gespeicherte(context)?.let { name(context, it) }

    fun speichern(context: Context, karte: PhoneAccountHandle) {
        context.getSharedPreferences(DATEI, Context.MODE_PRIVATE).edit {
            putString(KARTE, karte.id)
        }
    }

    /**
     * Zeigt die Auswahl der Karten.
     *
     * Bewusst hier und nicht in den Activities: Derselbe Dialog stand am
     * 2026-10-08 an zwei Stellen, und beim Umstellen auf lesbare Namen blieb
     * eine davon auf den blanken Kennungen stehen.
     *
     * Gibt `false` zurueck, wenn es nichts zu waehlen gibt.
     */
    fun auswahlZeigen(
        activity: android.app.Activity,
        danach: () -> Unit,
    ): Boolean {
        val karten = verfuegbare(activity)
        if (karten.isEmpty()) return false

        val namen = karten.map { name(activity, it) }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle(R.string.karte_label)
            .setItems(namen) { _, gewaehlt ->
                speichern(activity, karten[gewaehlt])
                danach()
            }
            .show()
        return true
    }

    /**
     * Der Anruf-Intent - mit Karte, falls eine feststeht.
     *
     * Ohne das Extra fragt Android auf Zweikartengeraeten zurueck.
     */
    fun anruf(context: Context, nummer: String, waehlen: Boolean): Intent {
        val aktion = if (waehlen) Intent.ACTION_CALL else Intent.ACTION_DIAL
        val absicht = Intent(aktion, Uri.fromParts("tel", nummer, null))
        gespeicherte(context)?.let {
            absicht.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, it)
        }
        return absicht
    }
}
