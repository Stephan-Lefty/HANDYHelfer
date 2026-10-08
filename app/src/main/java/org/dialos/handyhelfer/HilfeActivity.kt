package org.dialos.handyhelfer

import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import org.dialos.handyhelfer.databinding.ActivityHilfeBinding

class HilfeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindung = ActivityHilfeBinding.inflate(layoutInflater)
        setContentView(bindung.root)
        bindung.root.raenderBeachten()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bindung.hilfeText.text =
            Html.fromHtml(getString(R.string.hilfe_text), Html.FROM_HTML_MODE_COMPACT)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
