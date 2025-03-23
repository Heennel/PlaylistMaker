package com.example.playlistmaker.Activity

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
import com.example.playlistmaker.API.TrackResponse
import com.example.playlistmaker.API.iTunesApi
import com.example.playlistmaker.R
import com.example.playlistmaker.API.Track
import com.example.playlistmaker.RecyclerView.ClickListener
import com.example.playlistmaker.RecyclerView.HistoryAdapter
import com.example.playlistmaker.RecyclerView.ListTrackUpdater
import com.example.playlistmaker.RecyclerView.TrackAdapter
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

private const val TEXT_KEY = "TEXT_KEY"
private const val HISTORY_LIST_KEY = "HISTORY_LIST_KEY"
private const val base_url = "https://itunes.apple.com"

class SearchActivity : AppCompatActivity(), ClickListener {

    private lateinit var editText: EditText
    private lateinit var arrowBack: ImageView
    private lateinit var clearButton: ImageView
    private lateinit var recyclerTrack: RecyclerView

    private lateinit var notFoundImage: ImageView
    private lateinit var notFoundText: TextView

    private lateinit var connectionFailedImage: ImageView
    private lateinit var connectoinFailedText: TextView
    private lateinit var reloadButton: Button
    private lateinit var problemsText: TextView

    private lateinit var historyRV: RecyclerView
    private lateinit var clearHistoryButton: Button
    private lateinit var historyText: TextView

    private var trackLastName = ""

    private var adapter = TrackAdapter(this)
    private var historyAdapter = HistoryAdapter(this)

    private var trackList = ArrayList<Track>()

    private val retrofit = Retrofit.Builder()
        .baseUrl(base_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val iTunesApiService = retrofit.create<iTunesApi>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        notFoundImage = findViewById(R.id.notFoundImage)
        notFoundText = findViewById(R.id.notFoundText)
        connectionFailedImage = findViewById(R.id.connectionFailedImage)
        connectoinFailedText = findViewById(R.id.connectionFailedText)
        reloadButton = findViewById(R.id.reloadButton)
        problemsText = findViewById(R.id.problemsText)

        historyRV = findViewById(R.id.historyRV)
        clearHistoryButton = findViewById(R.id.clearStoryButton)
        historyText = findViewById(R.id.historyText)

        arrowBack = findViewById(R.id.back)
        arrowBack.setOnClickListener {
            finish()
        }

        editText = findViewById(R.id.editTextSearch)
        clearButton = findViewById(R.id.resetButton)
        clearButton.visibility= View.INVISIBLE

        val sharedPreferences = getSharedPreferences(PLAYLIST_MAKER, MODE_PRIVATE)
        editText.setText(sharedPreferences.getString(TEXT_KEY,""))
        val historyList = sharedPreferences.getString(HISTORY_LIST_KEY,"")

        if(!historyList.isNullOrEmpty()) {
            historyAdapter.historyTrackList = createArrayFromJson(historyList).toCollection(ArrayList())
            historyAdapter.notifyDataSetChanged()
            showHistory()
        }

        if(editText.text.isNotEmpty()){
            clearButton.visibility = View.VISIBLE
        }

        clearButton.setOnClickListener {
            hideAll()
            editText.text.clear()
            this.currentFocus?.let { view ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }
            editText.clearFocus()
        }
        adapter.trackList = trackList

        recyclerTrack = findViewById(R.id.trackRV)
        recyclerTrack.layoutManager = LinearLayoutManager(this)
        recyclerTrack.adapter = adapter

        reloadButton.setOnClickListener {
            if (trackLastName.isNotEmpty()) {
                hideAll()
                getTrack(trackLastName)
            }
        }


        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    clearButton.visibility= View.INVISIBLE
                    hideAll()
                    if(historyAdapter.historyTrackList.isNotEmpty()){
                        showHistory()
                    }
                } else {
                    clearButton.visibility= View.VISIBLE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (editText.text.isNotEmpty()) {
                    getTrack(editText.text.toString())
                }
            }
            false
        }

        historyRV.adapter = historyAdapter
        historyRV.layoutManager = LinearLayoutManager(this)

        clearHistoryButton.setOnClickListener {
            historyAdapter.historyTrackList.clear()
            hideHistory()
            historyAdapter.notifyDataSetChanged()
            sharedPreferences.edit()
                .remove(HISTORY_LIST_KEY)
                .apply()
        }
    }



    private fun notFoundMessage() {
        notFoundText.visibility = View.VISIBLE
        notFoundImage.visibility = View.VISIBLE
        trackList.clear()
        adapter.notifyDataSetChanged()
    }
    private fun connectionFailedMessage(){
        connectionFailedImage.visibility = View.VISIBLE
        connectoinFailedText.visibility = View.VISIBLE
        reloadButton.visibility = View.VISIBLE
        problemsText.visibility = View.VISIBLE
        trackList.clear()
        adapter.notifyDataSetChanged()
    }
    private fun hideAll(){
        hidePlaceHolders()
        hideFoundRV()
    }
    private fun hideFoundRV(){
        recyclerTrack.visibility = View.GONE
    }
    private fun hidePlaceHolders(){
        connectionFailedImage.visibility = View.GONE
        connectoinFailedText.visibility = View.GONE
        reloadButton.visibility = View.GONE
        problemsText.visibility = View.GONE
        notFoundText.visibility = View.GONE
        notFoundImage.visibility = View.GONE
    }
    fun showHistory(){
        if(historyAdapter.historyTrackList.isNotEmpty()) {
            historyRV.visibility = View.VISIBLE
            clearHistoryButton.visibility = View.VISIBLE
            historyText.visibility = View.VISIBLE
        }
    }
    fun hideHistory(){
        historyRV.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
        historyText.visibility = View.GONE
    }





    private fun getTrack(trackName: String){
        trackLastName = trackName
        iTunesApiService.getTrack(trackName).enqueue(object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                hideHistory()
                if (response.isSuccessful) {
                    trackList.clear()
                    val responseList = response.body()?.results
                    if (!responseList.isNullOrEmpty()) {
                        recyclerTrack.visibility = View.VISIBLE
                        trackList.addAll(responseList)
                        adapter.notifyDataSetChanged()
                    }
                    if (trackList.isEmpty()) {
                        notFoundMessage()
                    }
                } else {
                    connectionFailedMessage()
                }
            }
            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                connectionFailedMessage()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        val sharedPreferences = getSharedPreferences(PLAYLIST_MAKER, MODE_PRIVATE)
        sharedPreferences.edit()
            .putString(TEXT_KEY,editText.text.toString())
            .apply()
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
}
