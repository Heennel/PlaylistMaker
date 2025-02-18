package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SearchActivity : AppCompatActivity() {
    private lateinit var editText: EditText
    private var searchLiveText = ""
    private val TEXT_KEY = "TEXT_KEY"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        val arrowBack = findViewById<ImageView>(R.id.back)

        arrowBack.setOnClickListener {
            finish()
        }

        val clearButton = findViewById<ImageView>(R.id.resetButton)
        clearButton.visibility= View.INVISIBLE

        clearButton.setOnClickListener {

            editText.text.clear()

            this.currentFocus?.let { view ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }

            editText.clearFocus()
        }

        editText = findViewById(R.id.editTextSearch)
        val resetIcon = editText.compoundDrawablesRelative[2]

        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
            editText.compoundDrawablesRelative[0],
            null,
            null,
            null
        )

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    clearButton.visibility= View.INVISIBLE
                } else {
                    editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        editText.compoundDrawablesRelative[0],
                        null,
                        resetIcon,
                        null
                    )
                    clearButton.visibility= View.VISIBLE
                }
                searchLiveText = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
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
}
