package com.gjsalot.books.utils

import android.text.Editable
import android.text.TextWatcher

fun SimpleTextWatcher(watcher: (text: String) -> Unit) = object : TextWatcher {
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun afterTextChanged(p0: Editable?) {}
    override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) = watcher(s.toString())
}
