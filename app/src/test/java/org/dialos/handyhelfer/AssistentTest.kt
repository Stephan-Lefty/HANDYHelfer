package org.dialos.handyhelfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistentTest {

    private fun leer() = Stand(
        fernhilfeInstalliert = false,
        bedienhilfeAktiv = false,
        fernhilfeKannSteuern = true,
        zusatzNoetig = false,
        zusatzDa = false,
        fernhilfeBrauchtPasswort = false,
        helferDa = false,
        anrufErlaubt = false,
        mehrereKarten = false,
        karteFestgelegt = false,
        balkenDa = false,
        symbolDa = false,
    )

    private fun fertig() = Stand(
        fernhilfeInstalliert = true,
        bedienhilfeAktiv = true,
        fernhilfeKannSteuern = true,
        zusatzNoetig = false,
        zusatzDa = false,
        fernhilfeBrauchtPasswort = false,
        helferDa = true,
        anrufErlaubt = true,
        mehrereKarten = false,
        karteFestgelegt = false,
        balkenDa = true,
        symbolDa = true,
    )

    private fun arten(s: Stand) = Assistent.schritte(s).map { it.art }

    @Test
    fun `ohne Fernhilfe wird nicht nach ihren Schaltern gefragt`() {
        // Sonst stuenden drei Schritte da, die sich erst beantworten lassen,
        // wenn der erste erledigt ist - das liest sich wie vier Probleme
        // statt wie eines.
        val liste = arten(leer())
        assertEquals(
            listOf(
                SchrittArt.FERNHILFE_INSTALLIEREN,
                SchrittArt.HELFER,
                SchrittArt.ANRUFEN_DUERFEN,
                SchrittArt.BALKEN,
                SchrittArt.SYMBOL,
            ),
            liste,
        )
    }

    @Test
    fun `das kleine Symbol ist Kuer`() {
        val schritt = Assistent.schritte(leer()).single { it.art == SchrittArt.SYMBOL }
        assertTrue(schritt.kuer)
    }

    @Test
    fun `fertig ist erst fertig, wenn alles Pflichtige sitzt`() {
        assertFalse(Assistent.fertig(leer()))
        assertTrue(Assistent.fertig(fertig()))
    }

    @Test
    fun `ohne das kleine Symbol gilt die Einrichtung trotzdem als fertig`() {
        assertTrue(Assistent.fertig(fertig().copy(symbolDa = false)))
    }

    @Test
    fun `der unpruefbare Schritt verhindert fertig nicht`() {
        // Sonst stuende nie "fertig" da: Ob jemand die eingeschraenkten
        // Einstellungen freigegeben hat, kann die App nicht nachsehen.
        // Laeuft die Bedienhilfe, hat er es offensichtlich getan.
        assertTrue(Assistent.fertig(fertig()))
    }

    @Test
    fun `fehlt die Bedienhilfe, ist die Einrichtung nicht fertig`() {
        assertFalse(Assistent.fertig(fertig().copy(bedienhilfeAktiv = false)))
    }

    @Test
    fun `fehlt der Helfer oder die Anruferlaubnis, ist nichts fertig`() {
        assertFalse(Assistent.fertig(fertig().copy(helferDa = false)))
        assertFalse(Assistent.fertig(fertig().copy(anrufErlaubt = false)))
    }

    @Test
    fun `die unpruefbare App-Pause verhindert fertig nicht`() {
        assertTrue(Assistent.fertig(fertig()))
    }

    @Test
    fun `nach der Karte wird nur bei zwei Karten ohne Standard gefragt`() {
        assertFalse(SchrittArt.KARTE_FUER_ANRUFE in arten(leer()))
        assertTrue(SchrittArt.KARTE_FUER_ANRUFE in arten(leer().copy(mehrereKarten = true)))
        // Steht eine Standardkarte fest, fragt Android nicht zurueck.
        assertFalse(
            SchrittArt.KARTE_FUER_ANRUFE in
                arten(leer().copy(mehrereKarten = true, karteFestgelegt = true)),
        )
    }

    @Test
    fun `eine offene Kartenwahl verhindert fertig`() {
        // Sonst stuende "alles sitzt" da, waehrend der grosse Knopf in einen
        // Auswahldialog fuehrt - genau der Fall vom 2026-10-08.
        assertFalse(Assistent.fertig(fertig().copy(mehrereKarten = true)))
        assertTrue(Assistent.fertig(fertig().copy(mehrereKarten = true, karteFestgelegt = true)))
    }

    @Test
    fun `Helfer und Anruferlaubnis tragen den Hauptzweck`() {
        // Ohne Fernhilfe bleibt der Anruf - ohne Anruf bleibt nichts.
        assertTrue(Assistent.blockiertHauptzweck(SchrittArt.HELFER))
        assertTrue(Assistent.blockiertHauptzweck(SchrittArt.ANRUFEN_DUERFEN))
        assertFalse(Assistent.blockiertHauptzweck(SchrittArt.BEDIENHILFE))
        assertFalse(Assistent.blockiertHauptzweck(SchrittArt.SYMBOL))
    }
}

/**
 * Was der Assistent aus der Wahl der Fernhilfe macht.
 *
 * Der Tag am Geraet (2026-10-08) hat gezeigt, dass die Wahl nicht frei ist:
 * Android laesst die Eingabesteuerung nur fuer Apps aus dem Play Store zu, und
 * die quelloffenen muessen sie dort weglassen. Diese Faelle halten fest, dass
 * die Liste das ehrlich sagt, statt einen Schalter anzubieten, der ausgegraut
 * bleibt.
 */
class FernhilfeartTest {

    private fun stand(
        kannSteuern: Boolean = true,
        zusatzNoetig: Boolean = false,
        zusatzDa: Boolean = false,
        passwort: Boolean = false,
    ) = Stand(
        fernhilfeInstalliert = true,
        bedienhilfeAktiv = false,
        fernhilfeKannSteuern = kannSteuern,
        zusatzNoetig = zusatzNoetig,
        zusatzDa = zusatzDa,
        fernhilfeBrauchtPasswort = passwort,
        helferDa = true,
        anrufErlaubt = true,
        mehrereKarten = false,
        karteFestgelegt = true,
        balkenDa = true,
        symbolDa = true,
    )

    private fun arten(s: Stand) = Assistent.schritte(s).map { it.art }

    @Test
    fun `kann die Fernhilfe nicht steuern, sagt die Liste das statt den Schalter anzubieten`() {
        val liste = arten(stand(kannSteuern = false))
        assertTrue(SchrittArt.NUR_ZUSEHEN in liste)
        assertFalse(SchrittArt.BEDIENHILFE in liste)
    }

    @Test
    fun `kann sie steuern, steht der Schalter da und nicht die Entschuldigung`() {
        val liste = arten(stand(kannSteuern = true))
        assertTrue(SchrittArt.BEDIENHILFE in liste)
        assertFalse(SchrittArt.NUR_ZUSEHEN in liste)
    }

    @Test
    fun `das Zusatzpaket steht vor der Bedienhilfe`() {
        // Ohne das Add-On gibt es den Schalter gar nicht, den der naechste
        // Schritt umlegen will.
        val liste = arten(stand(zusatzNoetig = true, zusatzDa = false))
        val zusatz = liste.indexOf(SchrittArt.ZUSATZ_INSTALLIEREN)
        val schalter = liste.indexOf(SchrittArt.BEDIENHILFE)
        assertTrue(zusatz in 0 until schalter)
    }

    @Test
    fun `ist das Zusatzpaket da, wird nicht mehr danach gefragt`() {
        assertFalse(SchrittArt.ZUSATZ_INSTALLIEREN in arten(stand(zusatzNoetig = true, zusatzDa = true)))
    }

    @Test
    fun `nach dem Passwort wird nur gefragt, wo es eines braucht`() {
        // TeamViewer und AnyDesk kommen ohne aus - dort bestaetigt die
        // Betroffene am Geraet. Ein Schritt, der ins Leere zeigt, verwirrt nur.
        assertFalse(SchrittArt.FESTES_PASSWORT in arten(stand(passwort = false)))
        assertTrue(SchrittArt.FESTES_PASSWORT in arten(stand(passwort = true)))
    }

    @Test
    fun `nur Zusehen verhindert fertig nicht`() {
        // Es ist kein offener Punkt, sondern eine Eigenschaft der Wahl. Wer
        // damit leben kann, ist fertig eingerichtet.
        assertTrue(Assistent.fertig(stand(kannSteuern = false)))
    }

    @Test
    fun `TeamViewer traegt das Add-On und braucht kein Passwort`() {
        val tv = Fernhilfeart.TEAMVIEWER
        assertTrue(tv.kannSteuern)
        assertFalse(tv.brauchtPasswort)
        assertEquals("com.teamviewer.quicksupport.addon.universal", tv.zusatzPaket)
    }

    @Test
    fun `die quelloffenen koennen nicht steuern - das ist der Befund des Tages`() {
        assertFalse(Fernhilfeart.RUSTDESK.kannSteuern)
        assertFalse(Fernhilfeart.HOPTODESK.kannSteuern)
        assertTrue(Fernhilfeart.RUSTDESK.quelloffen)
        assertTrue(Fernhilfeart.HOPTODESK.quelloffen)
    }
}
