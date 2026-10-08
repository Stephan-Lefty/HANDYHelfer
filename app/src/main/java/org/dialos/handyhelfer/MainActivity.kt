package org.dialos.handyhelfer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.dialos.handyhelfer.databinding.ActivityMainBinding
import org.dialos.handyhelfer.databinding.ZeileBefundBinding

/**
 * Der Startbildschirm: ein grosser Knopf, der Hilfe holt, ein kleinerer, der
 * die Fernhilfe oeffnet, und darunter der Zustandsbericht.
 *
 * Der Bericht wird **angezeigt und nicht verschickt**. Das klingt nach einer
 * Einschraenkung, ist aber der Grund, warum die App ohne Server, ohne Konto
 * und ohne eine einzige Netzwerkberechtigung auskommt: Der Helfer sieht den
 * Bildschirm ohnehin, sobald die Fernhilfe laeuft - also liest er ihn dort.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var bindung: ActivityMainBinding

    /** Nach der Freigabe sofort waehlen - sonst muesste die Betroffene zweimal tippen. */
    private val anruferlaubnis = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { erteilt -> if (erteilt) anrufen() else waehlerOeffnen() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindung = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindung.root)
        bindung.root.raenderBeachten()

        bindung.knopfHilfe.setOnClickListener { hilfeHolen() }
        bindung.knopfBildschirm.setOnClickListener { bildschirmFreigeben() }
        bindung.knopfEinrichtung.setOnClickListener {
            startActivity(Intent(this, EinrichtungActivity::class.java))
        }
        bindung.knopfProtokoll.setOnClickListener {
            startActivity(Intent(this, ProtokollActivity::class.java))
        }
        bindung.knopfHilfeText.setOnClickListener {
            startActivity(Intent(this, HilfeActivity::class.java))
        }
    }

    /**
     * Bei jeder Rueckkehr neu erheben. Der Zustand aendert sich waehrend der
     * App-Laufzeit - wer gerade den Flugmodus ausgeschaltet hat, soll das
     * sofort sehen und nicht erst nach einem Neustart der App.
     */
    override fun onResume() {
        super.onResume()
        if (!Helfer.eingerichtet(this)) {
            startActivity(Intent(this, EinrichtungActivity::class.java))
            return
        }
        anzeigen()
    }

    private fun anzeigen() {
        val helfer = Helfer.lesen(this) ?: return
        bindung.hilfeErklaerung.text = getString(R.string.hilfe_holen_erklaerung, helfer.name)
        bindung.bildschirmErklaerung.text =
            getString(R.string.bildschirm_freigeben_erklaerung, helfer.name)

        val zustand = Diagnose.erheben(this)
        bindung.geraet.text = getString(
            R.string.geraet_zeile,
            geraetName(zustand.hersteller, zustand.modell),
            zustand.androidVersion,
            BuildConfig.VERSION_NAME,
        )

        val befunde = Regeln.pruefen(zustand)
        bindung.befunde.removeAllViews()
        if (befunde.isEmpty()) {
            val zeile = ZeileBefundBinding.inflate(layoutInflater, bindung.befunde, true)
            zeile.zeichen.text = ""
            zeile.text.text = getString(R.string.alles_in_ordnung)
            return
        }
        for (befund in befunde) {
            val zeile = ZeileBefundBinding.inflate(layoutInflater, bindung.befunde, true)
            zeile.zeichen.text = BefundTexte.zeichen(befund.stufe)
            zeile.zeichen.setTextColor(ContextCompat.getColor(this, BefundTexte.farbe(befund.stufe)))
            zeile.text.text = BefundTexte.text(this, befund)
        }
    }

    private fun hilfeHolen() {
        val helfer = Helfer.lesen(this) ?: return
        Protokoll.anhaengen(this, Vorgang.HILFE_GEHOLT, helfer.name)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            anrufen()
        } else {
            anruferlaubnis.launch(Manifest.permission.CALL_PHONE)
        }
    }

    private fun anrufen() {
        val helfer = Helfer.lesen(this) ?: return
        startActivity(Karten.anruf(this, helfer.nummer, waehlen = true))
    }

    /**
     * Rueckfall ohne die Berechtigung: Die Nummer steht im Telefon, es fehlt
     * ein Tipp. Besser als eine App, die nach einem "Nein" nichts mehr tut.
     */
    private fun waehlerOeffnen() {
        val helfer = Helfer.lesen(this) ?: return
        Toast.makeText(this, R.string.anruf_nicht_erlaubt, Toast.LENGTH_LONG).show()
        startActivity(Karten.anruf(this, helfer.nummer, waehlen = false))
    }

    private fun bildschirmFreigeben() {
        val helfer = Helfer.lesen(this) ?: return
        if (!Fernhilfe.installiert(this)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.fernhilfe_fehlt_titel)
                .setMessage(R.string.fernhilfe_fehlt_text)
                .setPositiveButton(R.string.ja_oeffnen) { _, _ ->
                    Fernhilfe.installierenAnbieten(this)
                }
                .setNegativeButton(R.string.abbrechen, null)
                .show()
            return
        }
        Protokoll.anhaengen(this, Vorgang.FERNHILFE_GEOEFFNET, helfer.name)
        if (!Fernhilfe.oeffnen(this)) {
            Toast.makeText(this, R.string.fernhilfe_oeffnen_gescheitert, Toast.LENGTH_LONG).show()
        }
    }
}
