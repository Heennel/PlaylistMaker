package com.example.playlistmaker.RecyclerView

import com.example.playlistmaker.API.Track

class ListTrackUpdater(val trackList: ArrayList<Track>) {
    fun update(track: Track){
        if(trackList.size<10) {
            if (!trackList.contains(track)) {
                trackList.add(0, track)
            } else {
                trackList.remove(track)
                trackList.add(0,track)
            }
        }else{
            if(!trackList.contains(track)) {
                val lastSearchedTrack = trackList[9]
                trackList.remove(lastSearchedTrack)
                trackList.add(0,track)
            }else{
                trackList.remove(track)
                trackList.add(0,track)
            }
        }
    }
}