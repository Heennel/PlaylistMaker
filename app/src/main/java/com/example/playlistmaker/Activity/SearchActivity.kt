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
import com.example.playlistmaker.RecyclerView.TrackAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class SearchActivity : AppCompatActivity() {
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

    private var searchLiveText = ""
    private val base_url="https://itunes.apple.com"
    private val TEXT_KEY = "TEXT_KEY"
    private var trackLastName = ""

    private var adapter = TrackAdapter()

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

        arrowBack = findViewById(R.id.back)
        arrowBack.setOnClickListener {
            finish()
        }

        editText = findViewById(R.id.editTextSearch)
        clearButton = findViewById(R.id.resetButton)
        clearButton.visibility= View.INVISIBLE

        clearButton.setOnClickListener {
            recyclerTrack.visibility = View.INVISIBLE
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
        recyclerTrack.visibility = View.INVISIBLE
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
                } else {
                    clearButton.visibility= View.VISIBLE
                }
                searchLiveText = s.toString()
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

    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TEXT_KEY, searchLiveText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchLiveText = savedInstanceState.getString(TEXT_KEY, "")
        editText.setText(searchLiveText)
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
        connectionFailedImage.visibility = View.GONE
        connectoinFailedText.visibility = View.GONE
        reloadButton.visibility = View.GONE
        problemsText.visibility = View.GONE
        notFoundText.visibility = View.GONE
        notFoundImage.visibility = View.GONE
    }

    private fun getTrack(trackName: String){
        trackLastName = trackName
        iTunesApiService.getTrack(trackName).enqueue(object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                if (response.code() == 200) {
                    trackList.clear()
                    if (response.body()?.results?.isNotEmpty() == true) {
                        recyclerTrack.visibility = View.VISIBLE
                        trackList.addAll(response.body()?.results!!)
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
}
