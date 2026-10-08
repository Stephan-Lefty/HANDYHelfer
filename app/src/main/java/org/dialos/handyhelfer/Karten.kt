package org.dialos.handyhelfer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
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
     * Der Name, den das System der Karte gibt - "SIM 1", "eSIM", der
     * Anbietername.
     *
     * Die Kennung (`handle.id`) waere eine technische Zeichenkette und als
     * Auswahl unbrauchbar: Wer zwischen zwei Zeilen Kauderwelsch waehlen soll,
     * waehlt gar nicht.
     */
    fun name(context: Context, karte: PhoneAccountHandle): String {
        val konto = try {
            telecom(context).getPhoneAccount(karte)
        } catch (_: SecurityException) {
            null
        }
        val beschriftung = konto?.label?.toString()?.trim().orEmpty()
        return beschriftung.ifEmpty { karte.id }
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
