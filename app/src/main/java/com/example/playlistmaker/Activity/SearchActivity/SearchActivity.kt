package com.example.playlistmaker.Activity.SearchActivity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.API.RetrofitFactory
import com.example.playlistmaker.API.TrackResponse
import com.example.playlistmaker.API.iTunesApi
import com.example.playlistmaker.R
import com.example.playlistmaker.API.Track
import com.example.playlistmaker.Activity.PLAYLIST_MAKER
import com.example.playlistmaker.RecyclerView.ClickListener
import com.example.playlistmaker.RecyclerView.HistoryAdapter
import com.example.playlistmaker.RecyclerView.ListTrackUpdater
import com.example.playlistmaker.RecyclerView.TrackAdapter
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create

private const val HISTORY_LIST_KEY = "HISTORY_LIST_KEY"
class SearchActivity : AppCompatActivity(), ClickListener {

    private lateinit var editTextTracks: EditText
    private lateinit var arrowBack: ImageView
    private lateinit var clearSearchButton: ImageView

    private lateinit var foundRV: RecyclerView

    private lateinit var notFoundImage: ImageView
    private lateinit var notFoundText: TextView
    private lateinit var reloadButton: Button

    private lateinit var connectionFailedImage: ImageView
    private lateinit var connectoinFailedText: TextView
    private lateinit var connectionProblemsText: TextView

    private lateinit var historyRV: RecyclerView
    private lateinit var clearHistoryButton: Button
    private lateinit var historyText: TextView

    private var activityStatus = SearchStatus.NOTHING

    private var foundAdapter = TrackAdapter(this)
    private var historyAdapter = HistoryAdapter(this)

    private var trackList = ArrayList<Track>()

    private val iTunesApiService = RetrofitFactory.createRetrofit().create<iTunesApi>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        notFoundImage = findViewById(R.id.notFoundImage)
        notFoundText = findViewById(R.id.notFoundText)
        connectionFailedImage = findViewById(R.id.connectionFailedImage)
        connectoinFailedText = findViewById(R.id.connectionFailedText)
        reloadButton = findViewById(R.id.reloadButton)
        connectionProblemsText = findViewById(R.id.connectionProblemsText)
        historyRV = findViewById(R.id.historyRV)
        foundRV = findViewById(R.id.trackRV)
        clearSearchButton = findViewById(R.id.clearButton)
        clearHistoryButton = findViewById(R.id.clearStoryButton)
        historyText = findViewById(R.id.historyText)
        editTextTracks = findViewById(R.id.editTextSearch)
        arrowBack = findViewById(R.id.back)

        historyRV.adapter = historyAdapter
        foundRV.adapter = foundAdapter
        historyRV.layoutManager = LinearLayoutManager(this)
        foundRV.layoutManager = LinearLayoutManager(this)
        foundAdapter.trackList = trackList

        val sharedPreferences = getSharedPreferences(PLAYLIST_MAKER, MODE_PRIVATE)
        val historyList = sharedPreferences.getString(HISTORY_LIST_KEY,"")
        if(!historyList.isNullOrEmpty()) {
            historyAdapter.historyTrackList = createArrayFromJson(historyList).toCollection(ArrayList())
            historyAdapter.notifyDataSetChanged()
            setActivityStatus(SearchStatus.HISTORY_TRACK_LIST)
        }

        arrowBack.setOnClickListener {
            finish()
        }
        if(editTextTracks.text.isNotEmpty()){
            clearSearchButton.visibility = View.VISIBLE
        }

        editTextTracks.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    clearSearchButton.visibility= View.INVISIBLE
                    if(historyAdapter.historyTrackList.isNotEmpty()){
                        setActivityStatus(SearchStatus.HISTORY_TRACK_LIST)
                    }
                } else {
                    clearSearchButton.visibility= View.VISIBLE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        editTextTracks.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (editTextTracks.text.isNotEmpty()) {
                    getTrack()
                }
            }
            false
        }
        clearSearchButton.setOnClickListener {
            setActivityStatus(SearchStatus.NOTHING)
            editTextTracks.text.clear()
            this.currentFocus?.let { view ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }
            editTextTracks.clearFocus()
        }

        clearHistoryButton.setOnClickListener {
            historyAdapter.historyTrackList.clear()
            hideHistory()
            historyAdapter.notifyDataSetChanged()
            sharedPreferences.edit()
                .remove(HISTORY_LIST_KEY)
                .apply()
        }
        reloadButton.setOnClickListener {
            getTrack()
        }
    }
    fun hideHistory(){
        historyRV.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
        historyText.visibility = View.GONE
    }

    private fun getTrack(){
        val trackName = editTextTracks.text.toString()
        iTunesApiService.getTrack(trackName).enqueue(object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                if (response.isSuccessful) {
                    trackList.clear()
                    val responseList = response.body()?.results
                    if (!responseList.isNullOrEmpty()) {
                        trackList.addAll(responseList)
                        foundAdapter.notifyDataSetChanged()
                        setActivityStatus(SearchStatus.FOUND_TRACK_LIST)
                    }
                    if (trackList.isEmpty()) {
                        setActivityStatus(SearchStatus.FAILED_NO_FOUND)
                    }
                } else {
                    setActivityStatus(SearchStatus.FAILED_NO_INTERNET)
                }
            }
            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                setActivityStatus(SearchStatus.FAILED_NO_INTERNET)
            }
        })
    }
    override fun onClick(track: Track) {
        val listUpdater = ListTrackUpdater(historyAdapter.historyTrackList)
        val sharedPreferences = getSharedPreferences(PLAYLIST_MAKER, MODE_PRIVATE)
        listUpdater.update(track)
        historyAdapter.notifyDataSetChanged()
        sharedPreferences.edit()
            .putString(HISTORY_LIST_KEY,createJsonFromArray(historyAdapter.historyTrackList.toArray()))
            .apply()
    }
    fun createArrayFromJson(json: String):Array<Track>{
        return Gson().fromJson(json, Array<Track>::class.java)
    }
    fun createJsonFromArray(array: Array<Any>):String{
        return Gson().toJson(array)
    }
    fun setActivityStatus(status: SearchStatus){
        when(status){
            SearchStatus.NOTHING -> {
                foundRV.visibility = View.GONE

                notFoundImage.visibility = View.GONE
                notFoundText.visibility = View.GONE

                reloadButton.visibility = View.GONE
                connectoinFailedText.visibility = View.GONE
                connectionFailedImage.visibility = View.GONE
                connectionProblemsText.visibility = View.GONE

                historyText.visibility = View.GONE
                historyRV.visibility = View.GONE
                clearHistoryButton.visibility = View.GONE
            }
            SearchStatus.FOUND_TRACK_LIST -> {
                foundRV.visibility = View.VISIBLE

                notFoundImage.visibility = View.GONE
                notFoundText.visibility = View.GONE

                reloadButton.visibility = View.GONE
                connectoinFailedText.visibility = View.GONE
                connectionFailedImage.visibility = View.GONE
                connectionProblemsText.visibility = View.GONE

                historyText.visibility = View.GONE
                historyRV.visibility = View.GONE
                clearHistoryButton.visibility = View.GONE
            }

            SearchStatus.FAILED_NO_INTERNET -> {
                foundRV.visibility = View.GONE

                notFoundImage.visibility = View.GONE
                notFoundText.visibility = View.GONE

                reloadButton.visibility = View.VISIBLE
                connectoinFailedText.visibility = View.VISIBLE
                connectionFailedImage.visibility = View.VISIBLE
                connectionProblemsText.visibility = View.VISIBLE

                historyText.visibility = View.GONE
                historyRV.visibility = View.GONE
                clearHistoryButton.visibility = View.GONE
            }
            SearchStatus.FAILED_NO_FOUND -> {
                foundRV.visibility = View.GONE

                notFoundImage.visibility = View.VISIBLE
                notFoundText.visibility = View.VISIBLE

                reloadButton.visibility = View.GONE
                connectoinFailedText.visibility = View.GONE
                connectionFailedImage.visibility = View.GONE
                connectionProblemsText.visibility = View.GONE

                historyText.visibility = View.GONE
                historyRV.visibility = View.GONE
                clearHistoryButton.visibility = View.GONE
            }

            SearchStatus.HISTORY_TRACK_LIST -> {
                foundRV.visibility = View.GONE

                notFoundImage.visibility = View.GONE
                notFoundText.visibility = View.GONE

                reloadButton.visibility = View.GONE
                connectoinFailedText.visibility = View.GONE
                connectionFailedImage.visibility = View.GONE
                connectionProblemsText.visibility = View.GONE

                historyText.visibility = View.VISIBLE
                historyRV.visibility = View.VISIBLE
                clearHistoryButton.visibility = View.VISIBLE
            }
        }
    }
}
