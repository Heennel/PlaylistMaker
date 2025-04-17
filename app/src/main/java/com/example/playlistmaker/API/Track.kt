package com.example.playlistmaker.API

import android.os.Parcelable
import androidx.annotation.NonNull
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Track(val trackName: String,
                 val artistName: String,
                 val country: String,
                 @SerializedName("trackTimeMillis") val trackTime: String,
                 @SerializedName("artworkUrl100") val trackImage: String,
                 @SerializedName("collectionName") val albumName: String,
                 @SerializedName("releaseDate") val yearNumber: String,
                 @SerializedName("primaryGenreName") val genre: String,
                 @SerializedName("previewUrl") val music_url: String
    ):Parcelable
