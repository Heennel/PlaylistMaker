package com.example.playlistmaker

import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener{
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }
        val toAgreement = findViewById<ImageView>(R.id.toAgreement)
        toAgreement.setOnClickListener{
            val url = Uri.parse("https://yandex.ru/legal/practicum_offer/")
            val launchBrowser = Intent(Intent.ACTION_VIEW, url)
            startActivity(launchBrowser)
        }
        val supportButton = findViewById<ImageView>(R.id.supportButton)
        supportButton.setOnClickListener{

            val message = "Спасибо разработчикам и разработчицам за крутое приложение!"
            val title = "Сообщение разработчикам и разработчицам приложения Playlist Maker"
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
            val shareText = "Стань Android-разработчиком: https://praktikum.yandex.ru/android-developer/"

            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Android Development Course")
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)

            startActivity(shareIntent)
        }
    }
}