package com.gjsalot.books.utils

import android.text.Html
import android.text.Spanned

object StringUtils {

    @Suppress("DEPRECATION")
    fun fromHtml(html: String): Spanned {
        val result: Spanned
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            result = Html.fromHtml(html)
        }
        return result
    }

}
