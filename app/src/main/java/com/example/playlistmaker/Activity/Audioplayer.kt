package com.example.playlistmaker.Activity

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.playlistmaker.API.Track
import com.example.playlistmaker.R
import java.util.Locale

class Audioplayer : AppCompatActivity() {
    private lateinit var backButton: ImageButton

    private lateinit var trackNameTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var yearNubmerTextView: TextView
    private lateinit var trackImageView: ImageView
    private lateinit var durationTrackTextView: TextView
    private lateinit var genreTextView: TextView
    private lateinit var countryTextView: TextView
    private lateinit var albumTextView: TextView
    private lateinit var albumNameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audioplayer)
        initViews()
        backButton.setOnClickListener {
            finish()
        }
        val track = IntentCompat.getParcelableExtra(intent,"TRACK",Track::class.java)?:return

        val trackName = track.trackName
        val artistName = track.artistName
        val yearNumber = track.yearNumber.substring(0,4)
        val trackImage = track.trackImage.replaceAfterLast('/',"1024x1024bb.jpg")
        val durationTrackBad = track.trackTime
        val durationTrackGood = SimpleDateFormat("mm:ss", Locale.getDefault()).format(durationTrackBad.toLong())
        val genre = track.genre
        val country = track.country
        val albumName = track.albumName

        trackNameTextView.setText(trackName)
        artistNameTextView.setText(artistName)
        yearNubmerTextView.setText(yearNumber)
        durationTrackTextView.setText(durationTrackGood)
        genreTextView.setText(genre)
        countryTextView.setText(country)
        albumNameTextView.setText(albumName)

        if(albumName.isNotEmpty()){
            albumNameTextView.setText(albumName)
            albumTextView.visibility = View.VISIBLE
            albumNameTextView.visibility = View.VISIBLE
        }else{
            albumTextView.visibility = View.GONE
            albumNameTextView.visibility = View.GONE
        }

        Glide.with(this)
            .load(trackImage)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(8)))
            .placeholder(R.drawable.placeholder_image)
            .into(trackImageView)
    }
    fun initViews(){
        backButton = findViewById(R.id.backButton)
        trackImageView = findViewById(R.id.trackImage)
        trackNameTextView = findViewById(R.id.trackNameText)
        artistNameTextView = findViewById(R.id.artistNameText)
        durationTrackTextView = findViewById(R.id.trackDurationTime)
        genreTextView = findViewById(R.id.genreName)
        yearNubmerTextView = findViewById(R.id.yearNumber)
        countryTextView = findViewById(R.id.countryName)
        albumTextView = findViewById(R.id.albumText)
        albumNameTextView = findViewById(R.id.albumName)
    }
}