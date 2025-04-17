package com.example.playlistmaker.RecyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.API.Track
import com.example.playlistmaker.R

class TrackAdapter( val listener: ClickListener
): RecyclerView.Adapter<TrackHolder>() {

    var trackList = ArrayList<Track>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_view, parent,false)
        return TrackHolder(view,parent.context)
    }
    override fun getItemCount(): Int {
        return trackList.size
    }
    override fun onBindViewHolder(holder: TrackHolder, position: Int) {
        holder.bind(trackList[position])
        holder.itemView.setOnClickListener {
            listener.onClick(trackList[position])
        }
    }
}