package com.example.playlistmaker

import android.content.Context
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        val switchDarkTheme = findViewById<Switch>(R.id.switchDarkTheme)
        switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener{
            finish()
        }
        val toAgreement = findViewById<ImageView>(R.id.toAgreement)
        toAgreement.setOnClickListener{
            val url = Uri.parse(resources.getString(R.string.yandex))
            val launchBrowser = Intent(Intent.ACTION_VIEW, url)
            startActivity(launchBrowser)
        }
        val supportButton = findViewById<ImageView>(R.id.supportButton)
        supportButton.setOnClickListener{

            val message = resources.getString(R.string.text_mail)
            val title = resources.getString(R.string.theme_mail)
            val supportIntent = Intent(Intent.ACTION_SENDTO)
            supportIntent.data = Uri.parse("mailto:")
            supportIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("paramo93@bk.ru"))
            supportIntent.putExtra(Intent.EXTRA_TEXT,message)
            supportIntent.putExtra(Intent.EXTRA_SUBJECT,title)
            startActivity(supportIntent)
        }

        val shareButton = findViewById<ImageView>(R.id.shareButton)
        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val shareText = resources.getString(R.string.share_text)

            shareIntent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.share_subject))
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)

            startActivity(shareIntent)
        }


    }
}