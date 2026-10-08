package org.dialos.handyhelfer

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.dialos.handyhelfer.databinding.ActivityProtokollBinding
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ProtokollActivity : AppCompatActivity() {

    private lateinit var bindung: ActivityProtokollBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindung = ActivityProtokollBinding.inflate(layoutInflater)
        setContentView(bindung.root)
        bindung.root.raenderBeachten()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val eintraege = Protokoll.lesen(this)
        if (eintraege.isEmpty()) {
            bindung.eintraege.addView(zeile(getString(R.string.protokoll_leer), gross = false))
            return
        }

        val format = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault())

        for (e in eintraege) {
            val was = when (e.vorgang) {
                Vorgang.HILFE_GEHOLT -> getString(R.string.vorgang_hilfe_geholt, e.helfer)
                Vorgang.FERNHILFE_GEOEFFNET -> getString(R.string.vorgang_fernhilfe, e.helfer)
                Vorgang.HELFER_GEAENDERT -> getString(R.string.vorgang_helfer_geaendert, e.helfer)
            }
            bindung.eintraege.addView(zeile(was, gross = true))
            bindung.eintraege.addView(zeile(format.format(e.zeitpunkt), gross = false))
        }
    }

    private fun zeile(text: String, gross: Boolean): TextView = TextView(this).apply {
        this.text = text
        textSize = if (gross) 18f else 15f
        setTextColor(
            ContextCompat.getColor(
                this@ProtokollActivity,
                if (gross) R.color.grau_dunkel else R.color.grau_leise,
            ),
        )
        setPadding(0, if (gross) 14 else 0, 0, 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
