package com.mv.livebodyexample

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.card.MaterialCardView

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        hideSystemUI()

        findViewById<MaterialCardView>(R.id.card_passive).setOnClickListener {
            startActivity(Intent(this, PassiveLivenessActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_active_blink).setOnClickListener {
            startActivity(Intent(this, BlinkTestActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_active_smile).setOnClickListener {
            startActivity(Intent(this, SmileTestActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_active_head).setOnClickListener {
            startActivity(Intent(this, HeadTurnTestActivity::class.java))
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
