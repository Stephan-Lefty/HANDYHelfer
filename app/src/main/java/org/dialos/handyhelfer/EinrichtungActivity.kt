package org.dialos.handyhelfer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.dialos.handyhelfer.databinding.ActivityEinrichtungBinding

/**
 * Hier wird der eine Helfer festgelegt - und nur hier.
 *
 * Der Bildschirm sagt im Klartext, warum es kein zweites Feld gibt. Das ist
 * kein Beiwerk: Die Masche, die diese App unbrauchbar machen koennte, besteht
 * darin, jemanden am Telefon zum Eintragen einer fremden Nummer zu ueberreden.
 * Wer die Begruendung einmal gelesen hat, wird bei genau dieser Bitte stutzig.
 */
class EinrichtungActivity : AppCompatActivity() {

    private lateinit var bindung: ActivityEinrichtungBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindung = ActivityEinrichtungBinding.inflate(layoutInflater)
        setContentView(bindung.root)
        bindung.root.raenderBeachten()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Helfer.lesen(this)?.let {
            bindung.feldName.setText(it.name)
            bindung.feldNummer.setText(it.nummer)
        }

        bindung.knopfSpeichern.setOnClickListener { speichern() }
        bindung.knopfStartbildschirm.setOnClickListener { aufStartbildschirm() }
        bindung.knopfSymbol.setOnClickListener { symbolDazu() }
    }

    private fun speichern(schliessen: Boolean = true): Boolean {
        val name = bindung.feldName.text.toString()
        val nummer = bindung.feldNummer.text.toString()

        if (name.isBlank()) {
            Toast.makeText(this, R.string.name_fehlt, Toast.LENGTH_LONG).show()
            return false
        }
        if (!Rufnummer.gueltig(nummer)) {
            Toast.makeText(this, R.string.nummer_unbrauchbar, Toast.LENGTH_LONG).show()
            return false
        }
        if (!Helfer.speichern(this, name, nummer)) {
            Toast.makeText(this, R.string.nummer_unbrauchbar, Toast.LENGTH_LONG).show()
            return false
        }

        // Ein Helferwechsel gehoert ins Protokoll. Wer spaeter nachsieht, wer
        // Zugriff hatte, muss auch sehen, wann sich das geaendert hat.
        Protokoll.anhaengen(this, Vorgang.HELFER_GEAENDERT, name.trim())
        // Sonst stuende im Widget noch der alte Name.
        HilfeWidgetProvider.erneuern(this)
        Toast.makeText(this, R.string.gespeichert, Toast.LENGTH_SHORT).show()
        if (schliessen) finish()
        return true
    }

    /**
     * Bietet an, den Knopf auf den Startbildschirm zu legen.
     *
     * Erst speichern, dann anbieten: Sonst stuende im Balken "Noch niemand
     * festgelegt", und der erste Eindruck waere ein kaputter Knopf.
     */
    private fun aufStartbildschirm() {
        if (!Helfer.eingerichtet(this) && !speichern(schliessen = false)) return

        if (Startbildschirm.knopfLiegtSchonDa(this)) {
            Toast.makeText(this, R.string.widget_schon_da, Toast.LENGTH_LONG).show()
            return
        }
        if (!Startbildschirm.knopfAblegenAnbieten(this)) {
            // Manche Hersteller-Oberflaechen und die meisten alternativen
            // Launcher koennen das nicht. Dann den Weg von Hand erklaeren,
            // statt stumm nichts zu tun.
            AlertDialog.Builder(this)
                .setMessage(R.string.widget_nicht_moeglich)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    /** Dasselbe fuer das quadratische Widget daneben. */
    private fun symbolDazu() {
        if (SymbolWidgetProvider.liegtSchonDa(this)) {
            Toast.makeText(this, R.string.widget_schon_da, Toast.LENGTH_LONG).show()
            return
        }
        if (!SymbolWidgetProvider.ablegenAnbieten(this)) {
            AlertDialog.Builder(this)
                .setMessage(R.string.widget_nicht_moeglich)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
