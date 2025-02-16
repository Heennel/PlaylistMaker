package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SearchActivity : AppCompatActivity() {
    private lateinit var editText: EditText
    private var searchLiveText = ""
    private val textToEditText = "Поиск"
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        val arrowBack = findViewById<ImageView>(R.id.back)

        arrowBack.setOnClickListener {
            val backIntent = Intent(this, MainActivity::class.java)
            startActivity(backIntent)
        }

        editText = findViewById(R.id.editTextSearch)
        val resetIcon = editText.compoundDrawablesRelative[2]

        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
            editText.compoundDrawablesRelative[0],
            null,
            null,
            null
        )

        //Как то с горем пополам с чужой помощью реализовал кнопку очищения, только потом понял что можно было наложить картинку на поле ввода =(
        editText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawables = editText.compoundDrawables
                if (drawables.size > 2) { //без этой проверки при нажатии на поле ввода активити вылетает
                    val resetIconDrawable = drawables[2]
                    if (resetIconDrawable != null) {
                        val resetIconWidth = resetIconDrawable.intrinsicWidth
                        if (event.rawX >= (editText.right - resetIconWidth)) {

                            //очистка поля ввода
                            editText.text.clear()

                            //скрыть клавиатуру
                            this.currentFocus?.let { view ->
                                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                imm?.hideSoftInputFromWindow(view.windowToken, 0)
                            }

                            //убрать курсор с текста
                            editText.clearFocus()
                            return@setOnTouchListener true
                        }
                    }
                }
            }
            false
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        editText.compoundDrawablesRelative[0],
                        null,
                        null,
                        null
                    )
                } else {
                    editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        editText.compoundDrawablesRelative[0],
                        null,
                        resetIcon,
                        null
                    )
                }
                searchLiveText = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(textToEditText, searchLiveText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchLiveText = savedInstanceState.getString(textToEditText, "")
        editText.setText(searchLiveText)
    }
}
