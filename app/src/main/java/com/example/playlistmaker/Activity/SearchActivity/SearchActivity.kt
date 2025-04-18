package com.example.playlistmaker.Activity.SearchActivity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
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
import com.example.playlistmaker.Activity.AudioplayerActivity.Audioplayer
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

class SearchActivity : AppCompatActivity(), ClickListener {

    companion object {
        private const val TRACK_KEY = "TRACK"
        private const val HISTORY_LIST_KEY = "HISTORY_LIST_KEY"
        private const val TEXT_KEY = "TEXT_KEY"
        private const val STATUS_KEY = "STATUS_KEY"
        private const val CLICK_DELAY = 1000L
        private const val REQUEST_DELAY = 2000L
    }
    private var isClickAllowed = true

    private val debounceHandler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { getTrack() }

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

    private lateinit var progressBar: ProgressBar

    private var activityStatus = SearchStatus.NOTHING

    private lateinit var sharedPreferences: SharedPreferences

    private var foundAdapter = TrackAdapter(this)
    private var historyAdapter = HistoryAdapter(this)

    private var trackList = ArrayList<Track>()

    private val iTunesApiService = RetrofitFactory.createRetrofit().create<iTunesApi>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        initViews()

        historyRV.adapter = historyAdapter
        foundRV.adapter = foundAdapter
        historyRV.layoutManager = LinearLayoutManager(this)
        foundRV.layoutManager = LinearLayoutManager(this)
        foundAdapter.trackList = trackList

        sharedPreferences = getSharedPreferences(PLAYLIST_MAKER, MODE_PRIVATE)
        val historyList = sharedPreferences.getString(HISTORY_LIST_KEY,"")
        if(!historyList.isNullOrEmpty()) {
            historyAdapter.historyTrackList = createArrayFromJson(historyList).toCollection(ArrayList())
            historyAdapter.notifyDataSetChanged()
        }

        setActivityStatus(SearchStatus.valueOf(sharedPreferences.getString(STATUS_KEY,"NOTHING")!!))
        editTextTracks.setText(sharedPreferences.getString(TEXT_KEY,""))

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
                    searchDebounce()
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
            historyAdapter.notifyDataSetChanged()
            setActivityStatus(SearchStatus.NOTHING)
            sharedPreferences.edit()
                .remove(HISTORY_LIST_KEY)
                .apply()
        }
        reloadButton.setOnClickListener {
            getTrack()
        }
    }

    private fun getTrack(){
        if(editTextTracks.text.isNotEmpty()) {
            setActivityStatus(SearchStatus.LOADING)
            val trackName = editTextTracks.text.toString()
            iTunesApiService.getTrack(trackName).enqueue(object : Callback<TrackResponse> {
                override fun onResponse(
                    call: Call<TrackResponse>,
                    response: Response<TrackResponse>
                ) {
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
    }


    override fun onPause() {
        super.onPause()
        sharedPreferences.edit()
            .putString(TEXT_KEY,editTextTracks.text.toString())
            .apply()
        sharedPreferences.edit()
            .putString(STATUS_KEY, activityStatus.name)
            .apply()
    }
    override fun onClick(track: Track) {
        if(clickAllower()) {
            toTrack(track)
            val listUpdater = ListTrackUpdater(historyAdapter.historyTrackList)
            listUpdater.update(track)
            historyAdapter.notifyDataSetChanged()
            sharedPreferences.edit()
                .putString(
                    HISTORY_LIST_KEY,
                    createJsonFromArray(historyAdapter.historyTrackList.toArray())
                )
                .apply()
        }
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
                activityStatus = SearchStatus.NOTHING
                progressBar.visibility = View.GONE
                foundRV.visibility = View.GONE

                noFoundMessageHide()
                connectionMessageHide()
                historyHide()
            }
            SearchStatus.FOUND_TRACK_LIST -> {
                activityStatus = SearchStatus.FOUND_TRACK_LIST
                foundRV.visibility = View.VISIBLE
                progressBar.visibility = View.GONE

                noFoundMessageHide()
                connectionMessageHide()
                historyHide()
            }

            SearchStatus.FAILED_NO_INTERNET -> {
                activityStatus = SearchStatus.FAILED_NO_INTERNET
                foundRV.visibility = View.GONE
                progressBar.visibility = View.GONE

                noFoundMessageHide()
                connectionMessageShow()
                historyHide()
            }
            SearchStatus.FAILED_NO_FOUND -> {
                activityStatus = SearchStatus.FAILED_NO_FOUND
                foundRV.visibility = View.GONE
                progressBar.visibility = View.GONE

                noFoundMessageShow()
                connectionMessageHide()
                historyHide()
            }

            SearchStatus.HISTORY_TRACK_LIST -> {
                activityStatus = SearchStatus.HISTORY_TRACK_LIST
                foundRV.visibility = View.GONE
                progressBar.visibility = View.GONE

                noFoundMessageHide()
                connectionMessageHide()
                historyShow()
            }
            SearchStatus.LOADING -> {
                activityStatus = SearchStatus.LOADING
                setActivityStatus(SearchStatus.NOTHING)
                progressBar.visibility = View.VISIBLE
            }
        }
    }
    fun toTrack(track: Track){
        val intent = Intent(this, Audioplayer::class.java).apply {
            putExtra(TRACK_KEY,track.copy(music_url = track.music_url ?: ""))
        }
        startActivity(intent)
    }
    fun clickAllower(): Boolean{
        val answer = isClickAllowed
        if(isClickAllowed){
            isClickAllowed = false
            debounceHandler.postDelayed({isClickAllowed = true}, CLICK_DELAY)
        }
        return answer
    }

    fun searchDebounce(){
        debounceHandler.removeCallbacks(searchRunnable)
        debounceHandler.postDelayed(searchRunnable,REQUEST_DELAY)
    }

    fun historyHide(){
        historyText.visibility = View.GONE
        historyRV.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
    }
    fun historyShow(){
        historyText.visibility = View.VISIBLE
        historyRV.visibility = View.VISIBLE
        clearHistoryButton.visibility = View.VISIBLE
    }
    fun connectionMessageHide(){
        reloadButton.visibility = View.GONE
        connectoinFailedText.visibility = View.GONE
        connectionFailedImage.visibility = View.GONE
        connectionProblemsText.visibility = View.GONE
    }
    fun connectionMessageShow(){
        reloadButton.visibility = View.VISIBLE
        connectoinFailedText.visibility = View.VISIBLE
        connectionFailedImage.visibility = View.VISIBLE
        connectionProblemsText.visibility = View.VISIBLE
    }
    fun noFoundMessageHide(){
        notFoundImage.visibility = View.GONE
        notFoundText.visibility = View.GONE
    }
    fun noFoundMessageShow(){
        notFoundImage.visibility = View.VISIBLE
        notFoundText.visibility = View.VISIBLE
    }

    fun initViews(){
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
        progressBar = findViewById(R.id.progressBar)
    }
}
