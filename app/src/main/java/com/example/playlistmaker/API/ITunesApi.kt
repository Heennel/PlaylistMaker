package com.example.playlistmaker.API

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface iTunesApi {
    @GET("/search?entity=song")
    fun getTrack(
        @Query("term", encoded = false) songName: String
    ): Call<TrackResponse>
}