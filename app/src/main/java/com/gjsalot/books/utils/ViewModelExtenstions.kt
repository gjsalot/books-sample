package com.gjsalot.books.utils

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.support.annotation.StringRes

fun AndroidViewModel.getString(@StringRes resId: Int): String =
        getApplication<Application>().getString(resId)