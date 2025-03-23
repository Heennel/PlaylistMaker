package com.example.playlistmaker.RecyclerView

import com.example.playlistmaker.API.Track

interface ClickListener {
    fun onClick(track: Track)
}