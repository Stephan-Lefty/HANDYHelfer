package org.dialos.handyhelfer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.dialos.handyhelfer.databinding.ActivityAssistentBinding
import org.dialos.handyhelfer.databinding.ZeileSchrittBinding

/**
 * Die Einrichtung, die sich selbst prueft.
 *
 * Gedacht fuer den Helfer, der einmal vor Ort ist - und spaeter fuer die
 * Frage "warum geht das nicht mehr?". Der Unterschied zu einer Anleitung zum
 * Abhaken: Wer hier einen Haken sieht, hat ihn nicht selbst gesetzt.
 *
 * Jeder Schritt oeffnet die Systemseite, auf der er stattfindet, statt zu
 * beschreiben, wo ein Schalter liegt. Menuepfade heissen bei Samsung, Xiaomi
 * und Motorola jeweils anders; ein Intent trifft ueberall.
 */
class AssistentActivity : AppCompatActivity() {

    private lateinit var bindung: ActivityAssistentBinding

    private val anruferlaubnis = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { zeichnen() }

    private val kartenerlaubnis = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { erteilt -> if (erteilt) karteWaehlen() else zeichnen() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindung = ActivityAssistentBinding.inflate(layoutInflater)
        setContentView(bindung.root)
        bindung.root.raenderBeachten()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Bei jeder Rueckkehr neu pruefen.
     *
     * Das ist der ganze Trick: Wer von den Bedienungshilfen zurueckkommt,
     * sieht sofort, ob es gewirkt hat - und muss nicht raten, ob er den
     * richtigen Schalter erwischt hat.
     */
    override fun onResume() {
        super.onResume()
        zeichnen()
    }

    private fun stand() = Stand(
        fernhilfeInstalliert = Fernhilfe.installiert(this),
        bedienhilfeAktiv = Systemseiten.bedienhilfeAktiv(this),
        helferDa = Helfer.eingerichtet(this),
        anrufErlaubt = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED,
        mehrereKarten = Karten.mehrereKarten(this),
        karteFestgelegt = Karten.festgelegt(this),
        balkenDa = Startbildschirm.knopfLiegtSchonDa(this),
        symbolDa = SymbolWidgetProvider.liegtSchonDa(this),
    )

    private fun zeichnen() {
        val stand = stand()
        bindung.assistentKopf.setText(
            if (Assistent.fertig(stand)) R.string.assistent_fertig else R.string.assistent_kopf,
        )

        bindung.schritte.removeAllViews()
        for (schritt in Assistent.schritte(stand)) {
            val zeile = ZeileSchrittBinding.inflate(layoutInflater, bindung.schritte, true)

            zeile.schrittZeichen.text = when {
                schritt.erledigt -> "✓"
                !schritt.pruefbar -> "?"
                else -> "○"
            }
            zeile.schrittZeichen.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (schritt.erledigt) R.color.gruen else R.color.grau_leise,
                ),
            )

            zeile.schrittTitel.setText(titel(schritt.art))
            zeile.schrittText.setText(erklaerung(schritt.art))

            // Erledigtes braucht keinen Knopf mehr - ausser bei dem Schritt,
            // den die App nicht nachsehen kann.
            if (schritt.erledigt) {
                zeile.schrittKnopf.visibility = android.view.View.GONE
            } else {
                zeile.schrittKnopf.setText(knopf(schritt.art))
                zeile.schrittKnopf.setOnClickListener { ausfuehren(schritt.art) }
            }
        }
    }

    private fun titel(art: SchrittArt) = when (art) {
        SchrittArt.FERNHILFE_INSTALLIEREN -> R.string.schritt_fernhilfe_titel
        SchrittArt.EINGESCHRAENKTE_EINSTELLUNGEN -> R.string.schritt_eingeschraenkt_titel
        SchrittArt.BEDIENHILFE -> R.string.schritt_bedienhilfe_titel
        SchrittArt.FESTES_PASSWORT -> R.string.schritt_passwort_titel
        SchrittArt.KARTE_FUER_ANRUFE -> R.string.schritt_karte_titel
        SchrittArt.HELFER -> R.string.schritt_helfer_titel
        SchrittArt.ANRUFEN_DUERFEN -> R.string.schritt_anrufen_titel
        SchrittArt.BALKEN -> R.string.schritt_balken_titel
        SchrittArt.SYMBOL -> R.string.schritt_symbol_titel
    }

    private fun erklaerung(art: SchrittArt) = when (art) {
        SchrittArt.FERNHILFE_INSTALLIEREN -> R.string.schritt_fernhilfe_text
        SchrittArt.EINGESCHRAENKTE_EINSTELLUNGEN -> R.string.schritt_eingeschraenkt_text
        SchrittArt.BEDIENHILFE -> R.string.schritt_bedienhilfe_text
        SchrittArt.FESTES_PASSWORT -> R.string.schritt_passwort_text
        SchrittArt.KARTE_FUER_ANRUFE -> R.string.schritt_karte_text
        SchrittArt.HELFER -> R.string.schritt_helfer_text
        SchrittArt.ANRUFEN_DUERFEN -> R.string.schritt_anrufen_text
        SchrittArt.BALKEN -> R.string.schritt_balken_text
        SchrittArt.SYMBOL -> R.string.schritt_symbol_text
    }

    private fun knopf(art: SchrittArt) = when (art) {
        SchrittArt.FERNHILFE_INSTALLIEREN -> R.string.schritt_knopf_seite
        SchrittArt.EINGESCHRAENKTE_EINSTELLUNGEN -> R.string.schritt_knopf_appinfo
        SchrittArt.BEDIENHILFE -> R.string.schritt_knopf_bedienungshilfen
        SchrittArt.FESTES_PASSWORT -> R.string.schritt_knopf_rustdesk
        SchrittArt.KARTE_FUER_ANRUFE -> R.string.schritt_knopf_karte
        SchrittArt.HELFER -> R.string.schritt_knopf_eintragen
        SchrittArt.ANRUFEN_DUERFEN -> R.string.schritt_knopf_erlauben
        SchrittArt.BALKEN, SchrittArt.SYMBOL -> R.string.schritt_knopf_ablegen
    }

    private fun ausfuehren(art: SchrittArt) {
        val geklappt = when (art) {
            SchrittArt.FERNHILFE_INSTALLIEREN -> Systemseiten.fernhilfeHolen(this)
            SchrittArt.EINGESCHRAENKTE_EINSTELLUNGEN -> Systemseiten.appInfoFernhilfe(this)
            SchrittArt.BEDIENHILFE -> Systemseiten.bedienungshilfen(this)
            SchrittArt.FESTES_PASSWORT -> Fernhilfe.oeffnen(this)
            SchrittArt.KARTE_FUER_ANRUFE -> karteWaehlen()

            SchrittArt.HELFER -> {
                startActivity(Intent(this, EinrichtungActivity::class.java))
                true
            }

            SchrittArt.ANRUFEN_DUERFEN -> {
                anruferlaubnis.launch(Manifest.permission.CALL_PHONE)
                true
            }

            SchrittArt.BALKEN -> Startbildschirm.knopfAblegenAnbieten(this)
            SchrittArt.SYMBOL -> SymbolWidgetProvider.ablegenAnbieten(this)
        }
        if (!geklappt) {
            Toast.makeText(this, R.string.schritt_ging_nicht, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Laesst die Karte fuer Anrufe waehlen.
     *
     * Erst die Berechtigung, dann die Liste: Ohne READ_PHONE_STATE ist sie
     * leer, und ein leerer Auswahldialog waere schlimmer als die Rueckfrage,
     * die wir loswerden wollen.
     */
    private fun karteWaehlen(): Boolean {
        if (!Karten.darfLesen(this)) {
            kartenerlaubnis.launch(Manifest.permission.READ_PHONE_STATE)
            return true
        }
        val karten = Karten.verfuegbare(this)
        if (karten.isEmpty()) return false

        val namen = karten.map { it.id }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.schritt_karte_titel)
            .setItems(namen) { _, gewaehlt ->
                Karten.speichern(this, karten[gewaehlt])
                zeichnen()
            }
            .show()
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
