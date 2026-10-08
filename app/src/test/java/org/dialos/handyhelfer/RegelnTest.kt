package org.dialos.handyhelfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Die Befundregeln, geprueft ohne Geraet und ohne Robolectric.
 *
 * Genau dafuer ist [Zustand] eine reine Datenklasse: Jeder Fall hier waere
 * am Telefon nur mit Muehe herzustellen - ein fast voller Speicher, ein
 * zwei Wochen nicht neu gestartetes Geraet, ein abgelaufenes Android.
 */
class RegelnTest {

    /** Ein Geraet, an dem nichts auffaellig ist. Basis fuer alle Faelle. */
    private fun heil() = Zustand(
        hersteller = "Motorola",
        modell = "edge 50 neo",
        androidVersion = "16",
        sdk = 36,
        akkuProzent = 80,
        akkuLaedt = false,
        akkusparmodus = false,
        speicherFreiMb = 20_000,
        speicherGesamtMb = 64_000,
        klingelLautstaerke = 10,
        klingelMaximum = 15,
        stummOderVibration = false,
        nichtStoeren = false,
        flugmodus = false,
        netzVerbunden = true,
        schriftSkalierung = 1.0f,
        betriebszeitStunden = 20,
        fernhilfeInstalliert = true,
    )

    private fun arten(z: Zustand) = Regeln.pruefen(z).map { it.art }

    @Test
    fun `ein heiles Geraet meldet nichts`() {
        assertEquals(emptyList<Art>(), arten(heil()))
    }

    @Test
    fun `Flugmodus ist eine Warnung`() {
        val befunde = Regeln.pruefen(heil().copy(flugmodus = true))
        assertEquals(listOf(Art.FLUGMODUS_AN), befunde.map { it.art })
        assertEquals(Stufe.WARNUNG, befunde.single().stufe)
    }

    @Test
    fun `im Flugmodus wird das fehlende Netz nicht doppelt gemeldet`() {
        // Sonst stuenden zwei Zeilen da, die dasselbe sagen - und die zweite
        // lenkt von der Ursache ab.
        val befunde = arten(heil().copy(flugmodus = true, netzVerbunden = false))
        assertEquals(listOf(Art.FLUGMODUS_AN), befunde)
    }

    @Test
    fun `stumm geschaltet ist eine Warnung`() {
        val befunde = Regeln.pruefen(heil().copy(stummOderVibration = true))
        assertEquals(listOf(Art.STUMM), befunde.map { it.art })
        assertEquals(Stufe.WARNUNG, befunde.single().stufe)
    }

    @Test
    fun `bei stumm geschaltetem Geraet zaehlt die Klingellautstaerke nicht`() {
        // Der Regler kann auf null stehen, solange das Geraet stumm ist. Beides
        // zu melden erzeugt zwei Zeilen fuer einen Umstand.
        val befunde = arten(heil().copy(stummOderVibration = true, klingelLautstaerke = 0))
        assertEquals(listOf(Art.STUMM), befunde)
    }

    @Test
    fun `leiser Klingelton ist ein Hinweis`() {
        val z = heil().copy(klingelLautstaerke = 3, klingelMaximum = 15)
        val befund = Regeln.pruefen(z).single()
        assertEquals(Art.KLINGELTON_LEISE, befund.art)
        assertEquals(Stufe.HINWEIS, befund.stufe)
        assertEquals(listOf("3", "15"), befund.werte)
    }

    @Test
    fun `ein Viertel Klingellautstaerke gilt noch als in Ordnung`() {
        // Grenzfall genau auf der Schwelle: 4 von 15 liegt ueber 25 Prozent.
        assertEquals(emptyList<Art>(), arten(heil().copy(klingelLautstaerke = 4, klingelMaximum = 15)))
    }

    @Test
    fun `fast voller Speicher ist eine Warnung mit Zahl`() {
        val befund = Regeln.pruefen(heil().copy(speicherFreiMb = 500)).single()
        assertEquals(Art.SPEICHER_FAST_VOLL, befund.art)
        assertEquals(Stufe.WARNUNG, befund.stufe)
        assertEquals(listOf("500"), befund.werte)
    }

    @Test
    fun `knapper Speicher zaehlt nur relativ zur Groesse`() {
        // 2 GB frei von 64 GB ist knapp ...
        assertTrue(Art.SPEICHER_KNAPP in arten(heil().copy(speicherFreiMb = 2_000, speicherGesamtMb = 64_000)))
        // ... 2 GB von 8 GB nicht, das sind ueber 10 Prozent.
        assertFalse(Art.SPEICHER_KNAPP in arten(heil().copy(speicherFreiMb = 2_000, speicherGesamtMb = 8_000)))
    }

    @Test
    fun `fast voll und knapp schliessen einander aus`() {
        val befunde = arten(heil().copy(speicherFreiMb = 500, speicherGesamtMb = 64_000))
        assertEquals(listOf(Art.SPEICHER_FAST_VOLL), befunde)
    }

    @Test
    fun `schwacher Akku am Kabel ist kein Befund`() {
        // Zehn Prozent beim Laden sind ein Zustand auf dem Weg nach oben.
        assertEquals(emptyList<Art>(), arten(heil().copy(akkuProzent = 10, akkuLaedt = true)))
        assertEquals(listOf(Art.AKKU_SCHWACH), arten(heil().copy(akkuProzent = 10, akkuLaedt = false)))
    }

    @Test
    fun `nach zwei Wochen ohne Neustart kommt der Hinweis mit Tagen`() {
        val befund = Regeln.pruefen(heil().copy(betriebszeitStunden = 15 * 24)).single()
        assertEquals(Art.LANGE_KEIN_NEUSTART, befund.art)
        assertEquals(listOf("15"), befund.werte)
    }

    @Test
    fun `altes Android wird mit seiner Nummer genannt`() {
        val befund = Regeln.pruefen(heil().copy(sdk = 28, androidVersion = "9")).single()
        assertEquals(Art.ANDROID_ALT, befund.art)
        assertEquals(listOf("9"), befund.werte)
    }

    @Test
    fun `fehlende Fernhilfe wird gemeldet`() {
        assertEquals(listOf(Art.FERNHILFE_FEHLT), arten(heil().copy(fernhilfeInstalliert = false)))
    }

    @Test
    fun `Warnungen stehen vor Hinweisen`() {
        val z = heil().copy(
            akkusparmodus = true,          // Hinweis
            stummOderVibration = true,     // Warnung
            fernhilfeInstalliert = false,  // Hinweis
            flugmodus = true,              // Warnung
        )
        val stufen = Regeln.pruefen(z).map { it.stufe }
        assertEquals(
            listOf(Stufe.WARNUNG, Stufe.WARNUNG, Stufe.HINWEIS, Stufe.HINWEIS),
            stufen,
        )
    }

    @Test
    fun `ein Geraet ohne Klingelstufen stuerzt nicht ab`() {
        // Division durch null waere hier der naheliegende Fehler.
        assertEquals(
            emptyList<Art>(),
            arten(heil().copy(klingelLautstaerke = 0, klingelMaximum = 0)),
        )
    }
}

/**
 * Hersteller und Modell zu einer Zeile.
 *
 * Die Faelle stammen aus echten Geraeten - erfundene haetten den Fehler nicht
 * gezeigt, der am 2026-10-08 auf dem Bildschirm stand.
 */
class GeraetNameTest {

    @Test
    fun `Motorola nennt sich im Modell schon selbst`() {
        // Genau das stand am 2026-10-08 als "Motorola motorola edge 50 neo" da.
        assertEquals("Motorola edge 50 neo", geraetName("motorola", "motorola edge 50 neo"))
    }

    @Test
    fun `Samsung meldet eine Typnummer und braucht den Hersteller davor`() {
        assertEquals("Samsung SM-A146P", geraetName("samsung", "SM-A146P"))
    }

    @Test
    fun `der Hersteller wird gross geschrieben`() {
        assertEquals("Xiaomi Redmi Note 12", geraetName("Xiaomi", "Redmi Note 12"))
        assertEquals("Google Pixel 8", geraetName("google", "Pixel 8"))
    }

    @Test
    fun `unterschiedliche Schreibweise zaehlt trotzdem als Dopplung`() {
        assertEquals("OnePlus 12", geraetName("OnePlus", "oneplus 12"))
    }

    @Test
    fun `fehlende Angaben stuerzen nicht ab`() {
        assertEquals("edge 50 neo", geraetName("", "edge 50 neo"))
        assertEquals("Motorola", geraetName("Motorola", ""))
        assertEquals("", geraetName("", ""))
    }
}
