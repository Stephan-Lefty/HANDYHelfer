package org.dialos.handyhelfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RufnummerTest {

    @Test
    fun `Leerzeichen und Trennzeichen fliegen raus`() {
        assertEquals("+491701234567", Rufnummer.normalisieren("+49 170 1234567"))
        assertEquals("+491701234567", Rufnummer.normalisieren("+49-170-1234567"))
        assertEquals("+491701234567", Rufnummer.normalisieren("+49 (170) 1234567"))
        assertEquals("+491701234567", Rufnummer.normalisieren(" +49/170/1234567 "))
    }

    @Test
    fun `fuehrende Doppelnull wird zum Plus`() {
        assertEquals("+491701234567", Rufnummer.normalisieren("0049 170 1234567"))
    }

    @Test
    fun `eine einzelne fuehrende Null bleibt stehen`() {
        // Daraus eine Landesvorwahl zu raten hiesse anzunehmen, Helfer und
        // Geraet saessen im selben Land. Bei einer Tochter in Deutschland und
        // einer Mutter in Oesterreich waere die Annahme falsch - und der Anruf
        // ginge an eine fremde Nummer.
        assertEquals("01701234567", Rufnummer.normalisieren("0170 1234567"))
    }

    @Test
    fun `oesterreichische Nummern gehen genauso`() {
        assertEquals("+436641234567", Rufnummer.normalisieren("+43 664 1234567"))
    }

    @Test
    fun `leere und unbrauchbare Eingaben ergeben null`() {
        assertNull(Rufnummer.normalisieren(null))
        assertNull(Rufnummer.normalisieren(""))
        assertNull(Rufnummer.normalisieren("   "))
        assertNull(Rufnummer.normalisieren("Thomas"))
        assertNull(Rufnummer.normalisieren("1234"))
    }

    @Test
    fun `zu lange Ziffernfolgen gelten nicht`() {
        // E.164 kennt hoechstens 15 Ziffern. Was laenger ist, ist ein Vertipper.
        assertNull(Rufnummer.normalisieren("+49170123456789012"))
    }

    @Test
    fun `gueltig stimmt mit normalisieren ueberein`() {
        assertTrue(Rufnummer.gueltig("+49 170 1234567"))
        assertFalse(Rufnummer.gueltig("abc"))
        assertFalse(Rufnummer.gueltig(null))
    }
}
