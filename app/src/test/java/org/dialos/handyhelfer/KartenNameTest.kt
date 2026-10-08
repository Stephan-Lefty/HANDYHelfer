package org.dialos.handyhelfer

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Welcher Name in der Kartenauswahl landet.
 *
 * Die Faelle stammen vom Testgeraet (Motorola edge 50 neo, SIM plus eSIM) am
 * 2026-10-08. Erfundene haetten den Fehler nicht gezeigt: Die Auswahl zeigte
 * dort "4" und "3", weil das Label der Telefonkonten leer ist.
 */
class KartenNameTest {

    @Test
    fun `der Anzeigename des Vertrags gewinnt, wenn das Label leer ist`() {
        assertEquals(
            "1&1",
            Karten.besterName(
                label = "", anzeigename = "1&1", anbieter = "3", platz = 0, kennung = "4",
            ),
        )
    }

    @Test
    fun `ein Anbietername aus blanken Ziffern gilt als unbrauchbar`() {
        // Genau dieser Fall stand auf dem Geraet: carrierName war "3".
        // Ohne die Regel stuende in der Auswahl eine nackte Ziffer.
        assertEquals(
            "SIM 1",
            Karten.besterName(
                label = null, anzeigename = null, anbieter = "3", platz = 0, kennung = "4",
            ),
        )
    }

    @Test
    fun `der zweite Vertrag meldet seinen Anbieter brauchbar`() {
        assertEquals(
            "YELLLOW",
            Karten.besterName(
                label = null, anzeigename = "YELLLOW", anbieter = "YELLLOW",
                platz = 1, kennung = "3",
            ),
        )
    }

    @Test
    fun `ein vorhandenes Label hat Vorrang`() {
        assertEquals(
            "eSIM",
            Karten.besterName(
                label = "eSIM", anzeigename = "1&1", anbieter = "3", platz = 0, kennung = "4",
            ),
        )
    }

    @Test
    fun `ohne jeden Namen zaehlt der Steckplatz, menschlich gezaehlt`() {
        // Platz 0 ist fuer das System die erste Karte, fuer den Nutzer "SIM 1".
        assertEquals(
            "SIM 1",
            Karten.besterName(null, null, null, platz = 0, kennung = "4"),
        )
        assertEquals(
            "SIM 2",
            Karten.besterName(null, null, null, platz = 1, kennung = "3"),
        )
    }

    @Test
    fun `ohne alles bleibt die Kennung`() {
        assertEquals(
            "4",
            Karten.besterName(null, null, null, platz = -1, kennung = "4"),
        )
    }

    @Test
    fun `Leerzeichen zaehlen nicht als Name`() {
        assertEquals(
            "SIM 1",
            Karten.besterName("  ", "\t", " ", platz = 0, kennung = "4"),
        )
    }
}
