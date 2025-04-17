package com.example.playlistmaker.Activity.AudioplayerActivity

import android.icu.text.SimpleDateFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
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

    companion object {
        private const val CHECKTIME_DELAY = 490L

        private const val TRACK_KEY = "TRACK"
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
    }
    private var startWhenPreparing = false

    private lateinit var url: String

    private var mediaPlayer = MediaPlayer()

    private var playerState = STATE_DEFAULT

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var playButton: ImageButton

    private lateinit var timer: TextView
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
    private lateinit var progressBar: ProgressBar

    private val updateTimer = object : Runnable {
        override fun run() {
            val currentTime = mediaPlayer.currentPosition
            timer.text = SimpleDateFormat("m:ss", Locale.getDefault())
                .format(currentTime)
            handler.postDelayed(this, CHECKTIME_DELAY)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audioplayer)
        initViews()
        getTrackInfo()
        preparePlayer()

        backButton.setOnClickListener {
            finish()
        }

        playButton.setOnClickListener{
            playbackControl()
        }
    }

    private fun preparePlayer() {
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            playButton.isEnabled = true
            playerState = STATE_PREPARED
            if(startWhenPreparing){
                startPlayer()
                startWhenPreparing = false
                progressBar.visibility = View.GONE
                trackImageView.visibility = View.VISIBLE
            }
        }
        mediaPlayer.setOnCompletionListener {
            playerState = STATE_PREPARED
            playButton.setImageResource(R.drawable.play)
            timer.text = resources.getString(R.string.null_time_track_duration)
            handler.removeCallbacks(updateTimer)
        }
    }
    private fun playbackControl() {
        when(playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }
            STATE_PREPARED, STATE_PAUSED -> {
                playButton.setImageResource(R.drawable.play)
                startPlayer()
            }
            else -> {
                startWhenPreparing = true
                playButton.setImageResource(R.drawable.pause)
                trackImageView.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
            }
        }
    }
    private fun startPlayer() {
        mediaPlayer.start()
        playButton.setImageResource(R.drawable.pause)
        handler.post(updateTimer)
        playerState = STATE_PLAYING
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playButton.setImageResource(R.drawable.play)
        playerState = STATE_PAUSED
    }

    fun initViews(){
        progressBar = findViewById(R.id.progressBar)
        timer = findViewById(R.id.timer)
        playButton = findViewById(R.id.playButton)
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
    fun getTrackInfo(){
        val track = IntentCompat.getParcelableExtra(intent, TRACK_KEY,Track::class.java)?:return

        url = track.music_url
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

        val radiusInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8f,
            this.resources.displayMetrics
        ).toInt()
        Glide.with(this)
            .load(trackImage)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(radiusInPx)))
            .placeholder(R.drawable.placeholder_image)
            .into(trackImageView)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimer)
        mediaPlayer.release()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTimer)
        pausePlayer()
    }
}