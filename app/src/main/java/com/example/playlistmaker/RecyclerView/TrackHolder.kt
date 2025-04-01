package com.example.playlistmaker.RecyclerView

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.provider.CalendarContract.Colors
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.playlistmaker.API.Track
import com.example.playlistmaker.R
import com.google.android.material.imageview.ShapeableImageView
import java.util.Locale

class TrackHolder(item: View, val context: Context): RecyclerView.ViewHolder(item) {

    val trackImg = item.findViewById<ShapeableImageView>(R.id.imageTrack)
    val trackName = item.findViewById<TextView>(R.id.trackName)
    val trackDescription = item.findViewById<TextView>(R.id.trackArtist)
    val trackTime = item.findViewById<TextView>(R.id.track_time)
    fun bind(track: Track){

        val time = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTime.toLong())

        Glide.with(context)
            .load(track.trackImage)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(2)))
            .placeholder(R.drawable.placeholder_image)
            .into(trackImg)
        trackName.setText(track.trackName)
        trackDescription.setText(track.artistName)
        trackTime.setText(time)

    }
}