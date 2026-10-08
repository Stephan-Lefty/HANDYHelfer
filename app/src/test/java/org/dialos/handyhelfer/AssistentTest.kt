package org.dialos.handyhelfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistentTest {

    private fun leer() = Stand(
        fernhilfeInstalliert = false,
        bedienhilfeAktiv = false,
        fernhilfeAusBrowser = false,
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
        fernhilfeAusBrowser = false,
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
    fun `die eingeschraenkten Einstellungen stehen vor der Bedienhilfe`() {
        // Reihenfolge ist hier Inhalt: Der gesperrte Schalter ist die Ursache,
        // die fehlende Bedienhilfe nur die Folge.
        val liste = arten(leer().copy(fernhilfeInstalliert = true))
        val vorher = liste.indexOf(SchrittArt.EINGESCHRAENKTE_EINSTELLUNGEN)
        val nachher = liste.indexOf(SchrittArt.BEDIENHILFE)
        assertTrue(vorher in 0 until nachher)
    }

    @Test
    fun `laeuft die Bedienhilfe, verschwindet der unpruefbare Schritt`() {
        val liste = arten(leer().copy(fernhilfeInstalliert = true, bedienhilfeAktiv = true))
        assertFalse(SchrittArt.EINGESCHRAENKTE_EINSTELLUNGEN in liste)
        assertTrue(SchrittArt.BEDIENHILFE in liste)
    }

    @Test
    fun `der Schritt zu den eingeschraenkten Einstellungen gilt als unpruefbar`() {
        val schritt = Assistent.schritte(leer().copy(fernhilfeInstalliert = true))
            .single { it.art == SchrittArt.EINGESCHRAENKTE_EINSTELLUNGEN }
        assertFalse(schritt.pruefbar)
        assertFalse(schritt.erledigt)
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
    fun `aus dem Browser installiert fuehrt zu F-Droid statt zur App-Info`() {
        // Am 2026-10-08 am Geraet belegt: Bei Browser-Installation steht der
        // Schalter auf "Gesteuert durch eingeschraenkte Einstellung" und laesst
        // sich nicht antippen - und in der App-Info fehlt der Menuepunkt, mit
        // dem man das aufheben koennte. Dann ist Neuinstallieren der kuerzere
        // Weg, nicht das Suchen nach einem Schalter, den es nicht gibt.
        val ausBrowser = leer().copy(fernhilfeInstalliert = true, fernhilfeAusBrowser = true)
        assertTrue(SchrittArt.NEU_INSTALLIEREN_AUS_FDROID in arten(ausBrowser))
        assertFalse(SchrittArt.EINGESCHRAENKTE_EINSTELLUNGEN in arten(ausBrowser))
    }

    @Test
    fun `anders installiert bleibt es beim Hinweis auf die App-Info`() {
        val anders = leer().copy(fernhilfeInstalliert = true, fernhilfeAusBrowser = false)
        assertTrue(SchrittArt.EINGESCHRAENKTE_EINSTELLUNGEN in arten(anders))
        assertFalse(SchrittArt.NEU_INSTALLIEREN_AUS_FDROID in arten(anders))
    }

    @Test
    fun `laeuft die Bedienhilfe, ist die Herkunft gleichgueltig`() {
        // Dann ist die Frage beantwortet, egal wie sie beantwortet wurde.
        val laeuft = leer().copy(
            fernhilfeInstalliert = true, fernhilfeAusBrowser = true, bedienhilfeAktiv = true,
        )
        assertFalse(SchrittArt.NEU_INSTALLIEREN_AUS_FDROID in arten(laeuft))
        assertFalse(SchrittArt.EINGESCHRAENKTE_EINSTELLUNGEN in arten(laeuft))
    }

    @Test
    fun `die App-Pause steht, sobald die Fernhilfe da ist`() {
        assertFalse(SchrittArt.KEINE_APP_PAUSE in arten(leer()))
        assertTrue(SchrittArt.KEINE_APP_PAUSE in arten(leer().copy(fernhilfeInstalliert = true)))
    }

    @Test
    fun `die App-Pause gilt als unpruefbar`() {
        // isAutoRevokeWhitelisted beantwortet die Frage fuer fremde Pakete
        // nicht - am 2026-10-08 ausprobiert. Ein Fragezeichen ist ehrlicher
        // als ein Haken, der nichts belegt.
        val schritt = Assistent.schritte(leer().copy(fernhilfeInstalliert = true))
            .single { it.art == SchrittArt.KEINE_APP_PAUSE }
        assertFalse(schritt.pruefbar)
    }

    @Test
    fun `die unpruefbare App-Pause verhindert fertig nicht`() {
        assertTrue(Assistent.fertig(fertig()))
    }

    @Test
    fun `das feste Passwort wird erst nach der Installation gefragt`() {
        // Vorher gibt es nichts, worin man es setzen koennte.
        assertFalse(SchrittArt.FESTES_PASSWORT in arten(leer()))
        assertTrue(SchrittArt.FESTES_PASSWORT in arten(leer().copy(fernhilfeInstalliert = true)))
    }

    @Test
    fun `das feste Passwort bleibt stehen, auch wenn alles andere sitzt`() {
        // Anders als die eingeschraenkten Einstellungen verschwindet es nicht:
        // Es gibt keinen Folgeschritt, aus dessen Gelingen sich schliessen
        // liesse, dass es gesetzt ist.
        assertTrue(SchrittArt.FESTES_PASSWORT in arten(fertig()))
        val schritt = Assistent.schritte(fertig()).single { it.art == SchrittArt.FESTES_PASSWORT }
        assertFalse(schritt.pruefbar)
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
