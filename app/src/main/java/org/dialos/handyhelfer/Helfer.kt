package org.dialos.handyhelfer

import android.content.Context
import androidx.core.content.edit

/**
 * Der eine hinterlegte Helfer.
 *
 * **Genau einer, und er wird nicht im laufenden Betrieb gewechselt.** Das ist
 * keine Sparsamkeit, sondern der Kern des Schutzes: Die haeufigste Masche gegen
 * aeltere Menschen ist der Anruf eines angeblichen Mitarbeiters, der zur
 * Installation einer Fernwartung und zur Eingabe einer fremden Kennung
 * ueberredet. Wo es kein Eingabefeld fuer eine fremde Nummer gibt, laeuft diese
 * Ansprache ins Leere.
 *
 * Geaendert wird der Helfer deshalb nur in der Einrichtung, und die sagt
 * ausdruecklich, was sie bedeutet.
 */
data class Helfer(val name: String, val nummer: String) {

    companion object {
        private const val DATEI = "handyhelfer"
        private const val NAME = "helfer_name"
        private const val NUMMER = "helfer_nummer"

        fun lesen(context: Context): Helfer? {
            val p = context.getSharedPreferences(DATEI, Context.MODE_PRIVATE)
            val name = p.getString(NAME, null)?.trim().orEmpty()
            val nummer = Rufnummer.normalisieren(p.getString(NUMMER, null))
            if (name.isEmpty() || nummer == null) return null
            return Helfer(name, nummer)
        }

        /** Gibt zurueck, ob gespeichert wurde - die Nummer kann unbrauchbar sein. */
        fun speichern(context: Context, name: String, nummer: String): Boolean {
            val sauber = Rufnummer.normalisieren(nummer) ?: return false
            if (name.isBlank()) return false
            context.getSharedPreferences(DATEI, Context.MODE_PRIVATE).edit {
                putString(NAME, name.trim())
                putString(NUMMER, sauber)
            }
            return true
        }

        fun eingerichtet(context: Context): Boolean = lesen(context) != null
    }
}
