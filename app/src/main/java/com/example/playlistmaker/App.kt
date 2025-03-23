package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

const val PLAYLIST_MAKER = "Playlist Maker"
const val THEME_KEY = "theme_key"

class App: Application() {
    var darkTheme = false
    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = getSharedPreferences(PLAYLIST_MAKER, MODE_PRIVATE)
        val isDarkTheme = sharedPreferences.getBoolean(THEME_KEY, false)
        switchTheme(isDarkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean){
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}