package com.example.playlistmaker.API

import com.google.gson.annotations.SerializedName

data class Track(val trackName: String,
                 val artistName: String,
                 @SerializedName("trackTimeMillis") val trackTime: String,
                 @SerializedName("artworkUrl100") val trackImage: String
    )
