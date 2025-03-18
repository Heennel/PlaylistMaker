package com.example.playlistmaker.Activity

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.App
import com.example.playlistmaker.R
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView

const val PLAYLIST_MAKER = "Playlist Maker"
const val THEME_KEY = "theme_key"
const val SWITCH_KEY = "switch_key"

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        val sharedPref = getSharedPreferences(PLAYLIST_MAKER, MODE_PRIVATE)

        val switchDarkTheme = findViewById<SwitchMaterial>(R.id.switchDarkTheme)
        switchDarkTheme.isChecked = sharedPref.getBoolean(SWITCH_KEY,false)
        switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit()
                .putBoolean(THEME_KEY,isChecked)
                .apply()
            sharedPref.edit()
                .putBoolean(SWITCH_KEY,isChecked)
                .apply()
            (applicationContext as App).switchTheme(isChecked)
        }

        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener{
            finish()
        }
        val toAgreement = findViewById<MaterialTextView>(R.id.userAgreement)
        toAgreement.setOnClickListener{
            val url = Uri.parse(resources.getString(R.string.yandex))
            val launchBrowser = Intent(Intent.ACTION_VIEW, url)
            startActivity(launchBrowser)
        }
        val supportButton = findViewById<MaterialTextView>(R.id.support)
        supportButton.setOnClickListener{

            val message = resources.getString(R.string.text_mail)
            val title = resources.getString(R.string.theme_mail)
            val supportIntent = Intent(Intent.ACTION_SENDTO)
            val mail = resources.getString(R.string.mail)
            supportIntent.data = Uri.parse("mailto:")
            supportIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mail))
            supportIntent.putExtra(Intent.EXTRA_TEXT,message)
            supportIntent.putExtra(Intent.EXTRA_SUBJECT,title)
            startActivity(supportIntent)
        }

        val shareButton = findViewById<MaterialTextView>(R.id.share)
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