package org.dialos.handyhelfer

import android.content.Context

/**
 * Uebersetzt einen [Befund] in einen Satz.
 *
 * Die Trennung ist Absicht: [Regeln] kennen keine Zeichenketten und sind
 * deshalb ohne Geraet pruefbar; hier faellt nur noch das Nachschlagen an.
 */
object BefundTexte {

    private val ressourcen = mapOf(
        Art.FLUGMODUS_AN to R.string.befund_flugmodus_an,
        Art.KEIN_NETZ to R.string.befund_kein_netz,
        Art.STUMM to R.string.befund_stumm,
        Art.KLINGELTON_LEISE to R.string.befund_klingelton_leise,
        Art.NICHT_STOEREN to R.string.befund_nicht_stoeren,
        Art.SPEICHER_FAST_VOLL to R.string.befund_speicher_fast_voll,
        Art.SPEICHER_KNAPP to R.string.befund_speicher_knapp,
        Art.AKKU_SCHWACH to R.string.befund_akku_schwach,
        Art.AKKUSPARMODUS_AN to R.string.befund_akkusparmodus_an,
        Art.LANGE_KEIN_NEUSTART to R.string.befund_lange_kein_neustart,
        Art.SCHRIFT_SEHR_GROSS to R.string.befund_schrift_sehr_gross,
        Art.ANDROID_ALT to R.string.befund_android_alt,
        Art.FERNHILFE_FEHLT to R.string.befund_fernhilfe_fehlt,
    )

    fun text(context: Context, befund: Befund): String {
        val id = ressourcen[befund.art] ?: return befund.art.name
        return context.getString(id, *befund.werte.toTypedArray())
    }

    /**
     * Das Zeichen vor der Zeile.
     *
     * Farbe allein traegt keine Bedeutung - wer rot-gruen-blind ist, muss die
     * Stufe trotzdem erkennen. Dieselbe Regel gilt in POSTKutsche fuer die
     * Netzwerkkuerzel.
     */
    fun zeichen(stufe: Stufe): String = when (stufe) {
        Stufe.WARNUNG -> "!"
        Stufe.HINWEIS -> "·"
    }

    fun farbe(stufe: Stufe): Int = when (stufe) {
        Stufe.WARNUNG -> R.color.rot
        Stufe.HINWEIS -> R.color.grau_leise
    }
}
