package org.dialos.handyhelfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ProtokollTest {

    private val zeitpunkt = Instant.parse("2026-10-08T09:30:00Z")

    @Test
    fun `eine Zeile laesst sich wieder einlesen`() {
        val e = Eintrag(zeitpunkt, Vorgang.HILFE_GEHOLT, "Thomas")
        assertEquals(e, Protokoll.ausZeile(Protokoll.zeile(e)))
    }

    @Test
    fun `ein Trenner im Namen zerstoert die Zeile nicht`() {
        val e = Eintrag(zeitpunkt, Vorgang.FERNHILFE_GEOEFFNET, "Tho|mas")
        val zurueck = Protokoll.ausZeile(Protokoll.zeile(e))!!
        assertEquals("Tho mas", zurueck.helfer)
        assertEquals(Vorgang.FERNHILFE_GEOEFFNET, zurueck.vorgang)
    }

    @Test
    fun `ein Zeilenumbruch im Namen zerstoert die Datei nicht`() {
        // Sonst stuenden aus einem Eintrag zwei in der Datei, und der zweite
        // waere unlesbar.
        val e = Eintrag(zeitpunkt, Vorgang.HELFER_GEAENDERT, "Tho\nmas")
        assertEquals(1, Protokoll.zeile(e).lines().size)
    }

    @Test
    fun `beschaedigte Zeilen werden uebersprungen statt zu stuerzen`() {
        assertNull(Protokoll.ausZeile(""))
        assertNull(Protokoll.ausZeile("Unsinn"))
        assertNull(Protokoll.ausZeile("nicht-datum|HILFE_GEHOLT|Thomas"))
        assertNull(Protokoll.ausZeile("2026-10-08T09:30:00Z|GIBTS_NICHT|Thomas"))
        assertNull(Protokoll.ausZeile("2026-10-08T09:30:00Z|HILFE_GEHOLT"))
    }

    @Test
    fun `alle Vorgaenge ueberstehen den Umweg ueber die Zeile`() {
        for (v in Vorgang.entries) {
            val e = Eintrag(zeitpunkt, v, "Thomas")
            assertEquals(e, Protokoll.ausZeile(Protokoll.zeile(e)))
        }
    }
}
