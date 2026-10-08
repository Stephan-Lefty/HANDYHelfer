package org.dialos.handyhelfer

import android.os.Bundle
import android.widget.Toast
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
    }

    private fun speichern() {
        val name = bindung.feldName.text.toString()
        val nummer = bindung.feldNummer.text.toString()

        if (name.isBlank()) {
            Toast.makeText(this, R.string.name_fehlt, Toast.LENGTH_LONG).show()
            return
        }
        if (!Rufnummer.gueltig(nummer)) {
            Toast.makeText(this, R.string.nummer_unbrauchbar, Toast.LENGTH_LONG).show()
            return
        }
        if (!Helfer.speichern(this, name, nummer)) {
            Toast.makeText(this, R.string.nummer_unbrauchbar, Toast.LENGTH_LONG).show()
            return
        }

        // Ein Helferwechsel gehoert ins Protokoll. Wer spaeter nachsieht, wer
        // Zugriff hatte, muss auch sehen, wann sich das geaendert hat.
        Protokoll.anhaengen(this, Vorgang.HELFER_GEAENDERT, name.trim())
        Toast.makeText(this, R.string.gespeichert, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
