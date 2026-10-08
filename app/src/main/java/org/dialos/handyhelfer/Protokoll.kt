package org.dialos.handyhelfer

import android.content.Context
import java.io.File
import java.time.Instant
import java.time.format.DateTimeParseException

enum class Vorgang { HILFE_GEHOLT, FERNHILFE_GEOEFFNET, HELFER_GEAENDERT }

data class Eintrag(val zeitpunkt: Instant, val vorgang: Vorgang, val helfer: String)

/**
 * Was wann geschehen ist - am Geraet, im Klartext, fuer die Betroffene lesbar.
 *
 * Der Grund steht nicht in der Technik, sondern in der Zielgruppe: Wer einmal
 * auf einen falschen "Microsoft-Mitarbeiter" hereingefallen ist, kann
 * hinterher meist nicht sagen, was passiert ist. Diese Liste kann es. Sie
 * laesst sich aus der App heraus nicht loeschen; wer sie loeschen will, muss
 * die App loeschen.
 *
 * Sie verlaesst das Geraet nicht und wird nirgendwohin gemeldet.
 */
object Protokoll {

    private const val DATEI = "protokoll.txt"

    /** Darueber hinaus wird vorne abgeschnitten. Reicht fuer Jahre. */
    const val HOECHSTZAHL = 200

    private const val TRENNER = '|'

    /** Serialisierung - ohne Android, damit `ProtokollTest` ohne Geraet laeuft. */
    fun zeile(e: Eintrag): String {
        // Der Trenner darf im Namen nicht vorkommen, sonst zerfaellt die Zeile.
        val name = e.helfer.replace(TRENNER, ' ').replace('\n', ' ').trim()
        return "${e.zeitpunkt}$TRENNER${e.vorgang.name}$TRENNER$name"
    }

    /** Gibt `null` zurueck, wenn die Zeile unbrauchbar ist - beschaedigte Zeilen
     *  werden uebersprungen und nicht zum Absturz gemacht. */
    fun ausZeile(zeile: String): Eintrag? {
        val teile = zeile.split(TRENNER)
        if (teile.size < 3) return null
        val zeitpunkt = try {
            Instant.parse(teile[0])
        } catch (_: DateTimeParseException) {
            return null
        }
        val vorgang = Vorgang.entries.firstOrNull { it.name == teile[1] } ?: return null
        return Eintrag(zeitpunkt, vorgang, teile.drop(2).joinToString(TRENNER.toString()))
    }

    fun anhaengen(context: Context, vorgang: Vorgang, helfer: String, jetzt: Instant = Instant.now()) {
        val datei = File(context.filesDir, DATEI)
        val bisher = if (datei.exists()) datei.readLines() else emptyList()
        val neu = (bisher + zeile(Eintrag(jetzt, vorgang, helfer))).takeLast(HOECHSTZAHL)
        datei.writeText(neu.joinToString("\n"))
    }

    /** Neueste zuerst. */
    fun lesen(context: Context): List<Eintrag> {
        val datei = File(context.filesDir, DATEI)
        if (!datei.exists()) return emptyList()
        return datei.readLines().mapNotNull(::ausZeile).reversed()
    }
}
