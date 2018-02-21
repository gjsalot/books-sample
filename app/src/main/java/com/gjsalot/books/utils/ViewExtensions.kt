package com.gjsalot.books.utils

import android.view.View

var View.visible: Boolean
    get() = visibility == View.VISIBLE
    set(visible) {
        visibility = if (visible) View.VISIBLE else View.GONE
    }