package com.example.playlistmaker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class TrackAdapter(
    private val trackList: List<Track>
): RecyclerView.Adapter<TrackAdapter.TrackHolder>() {
    class TrackHolder(item: View,val context: Context): RecyclerView.ViewHolder(item) {

        val trackImg = item.findViewById<ShapeableImageView>(R.id.imageTrack)
        val trackName = item.findViewById<TextView>(R.id.trackName)
        val trackDescription = item.findViewById<TextView>(R.id.trackArtist)

        fun bind(track: Track){
            Glide.with(context).load(track.trackImg).into(trackImg)
            trackName.setText(track.trackName)
            trackDescription.setText("${track.artist}  · ${track.time}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_view, parent,false)
        return TrackHolder(view,parent.context)
    }

    override fun getItemCount(): Int {
        return trackList.size
    }

    override fun onBindViewHolder(holder: TrackHolder, position: Int) {
        holder.bind(trackList[position])
    }
}