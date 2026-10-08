package org.dialos.handyhelfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Die Nummern hier sind "Drama Numbers" der Bundesnetzagentur
 * (Mitteilung 148/2021): dauerhaft niemandem zugeteilt und ausdruecklich zur
 * Verwendung in Medien freigegeben. Eine ausgedachte Nummer kann dagegen
 * jemandem gehoeren - und in einem oeffentlichen Repository steht sie dann
 * fuer immer.
 */
class RufnummerTest {

    @Test
    fun `Leerzeichen und Trennzeichen fliegen raus`() {
        assertEquals("+4915228817386", Rufnummer.normalisieren("+49 152 28817386"))
        assertEquals("+4915228817386", Rufnummer.normalisieren("+49-152-28817386"))
        assertEquals("+4915228817386", Rufnummer.normalisieren("+49 (152) 28817386"))
        assertEquals("+4915228817386", Rufnummer.normalisieren(" +49/152/28817386 "))
    }

    @Test
    fun `fuehrende Doppelnull wird zum Plus`() {
        assertEquals("+4915228817386", Rufnummer.normalisieren("0049 152 28817386"))
    }

    @Test
    fun `eine einzelne fuehrende Null bleibt stehen`() {
        // Daraus eine Landesvorwahl zu raten hiesse anzunehmen, Helfer und
        // Geraet saessen im selben Land. Bei einer Tochter in Deutschland und
        // einer Mutter in Oesterreich waere die Annahme falsch - und der Anruf
        // ginge an eine fremde Nummer.
        assertEquals("015228817386", Rufnummer.normalisieren("0152 28817386"))
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
        assertNull(Rufnummer.normalisieren("+491522881738689012"))
    }

    @Test
    fun `gueltig stimmt mit normalisieren ueberein`() {
        assertTrue(Rufnummer.gueltig("+49 152 28817386"))
        assertFalse(Rufnummer.gueltig("abc"))
        assertFalse(Rufnummer.gueltig(null))
    }
}
