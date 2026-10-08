package org.dialos.handyhelfer

/**
 * Aufbereitung der Rufnummer des Helfers.
 *
 * Ohne Android-Bezug, damit die Faelle in `RufnummerTest` ohne Geraet laufen.
 * Bewusst keine Bibliothek wie libphonenumber: Hier wird genau eine Nummer
 * hinterlegt, und die traegt der Helfer selbst ein. Was wir brauchen, ist
 * Robustheit gegen Schreibweisen - nicht die Kenntnis aller Vorwahlen der Welt.
 */
object Rufnummer {

    /** Kuerzer ergibt keine waehlbare Nummer, laenger kennt die E.164 nicht. */
    private const val MIN_ZIFFERN = 5
    private const val MAX_ZIFFERN = 15

    /**
     * Macht aus einer eingetippten Nummer eine waehlbare.
     *
     * Entfernt Leerzeichen, Bindestriche, Schraegstriche und Klammern, wandelt
     * eine fuehrende Doppelnull in ein Plus. Gibt `null` zurueck, wenn daraus
     * keine plausible Nummer wird - dann soll die Oberflaeche nachfragen statt
     * einen Anruf ins Leere anzubieten.
     *
     * Eine fuehrende einzelne Null bleibt stehen: Daraus eine Landesvorwahl zu
     * raten ginge nur mit der Annahme, Helfer und Geraet saessen im selben Land.
     * Fuer eine Tochter in Deutschland und eine Mutter in Oesterreich waere die
     * Annahme falsch, und der Anruf ginge an eine fremde Nummer.
     */
    fun normalisieren(eingabe: String?): String? {
        if (eingabe.isNullOrBlank()) return null

        val roh = eingabe.trim()
        val plus = roh.startsWith("+") || roh.startsWith("00")
        val ziffern = roh.filter { it.isDigit() }

        val ohneDoppelnull = if (roh.startsWith("00")) ziffern.removePrefix("00") else ziffern
        if (ohneDoppelnull.length !in MIN_ZIFFERN..MAX_ZIFFERN) return null

        return if (plus) "+$ohneDoppelnull" else ohneDoppelnull
    }

    /** Ob aus der Eingabe eine waehlbare Nummer wird. */
    fun gueltig(eingabe: String?): Boolean = normalisieren(eingabe) != null
}
