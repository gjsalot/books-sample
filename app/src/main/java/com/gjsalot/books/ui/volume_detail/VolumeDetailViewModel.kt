package com.gjsalot.books.ui.volume_detail

import android.arch.lifecycle.*
import android.text.Spanned
import com.gjsalot.books.app.Application
import com.gjsalot.books.data.repo.VolumesRepo
import com.gjsalot.books.utils.SingleLiveEvent
import com.gjsalot.books.utils.StringUtils
import com.gjsalot.books.utils.plusAssign
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class VolumeDetailViewModel(
        private val volumeId: String,
        application: Application,
        private val volumesRepo: VolumesRepo,
        private val mainThread: Scheduler,
        private val computation: Scheduler
): AndroidViewModel(application) {

    data class ViewState(
            val thumbnailUrl: String?,
            val title: String,
            val authors: String,
            val publishDetails: String,
            val pages: String,
            val description: CharSequence,
            val averageRating: Float?,
            val numRatings: Int?,
            val isBuyVisible: Boolean
    )

    sealed class Event {
        data class LaunchWebBrowser(val url: String): Event()
        object ShowErrorLoading: Event()
    }

    private val disposable = CompositeDisposable()

    private val viewState = MutableLiveData<ViewState>()
    fun viewState(): LiveData<ViewState> = viewState

    private val event = SingleLiveEvent<Event>()
    fun event(): LiveData<Event> = event

    private var amazonUrl: String? = null

    init {
        loadData()
    }

    private fun loadData() {
        disposable += volumesRepo
                .volume(volumeId)
                .observeOn(computation)
                .map {
                    amazonUrl = it.abeBooksUrl

                    ViewState(
                            it.thumbnailUrl,
                            it.title,
                            it.authors.joinToString(", "),
                            "${it.publisher}, ${it.publishedDate}",
                            "${it.pages} pages",
                            StringUtils.fromHtml(it.descriptionHtml).trim(),
                            it.averageRating,
                            it.numRatings,
                            !it.abeBooksUrl.isNullOrBlank()
                    )
                }
                .observeOn(mainThread)
                .subscribe({
                    viewState.value = it
                }, {
                    event.value = Event.ShowErrorLoading
                })
    }

    fun onRetryClicked() {
        loadData()
    }

    fun onBuyOrRentClicked() {
        amazonUrl?.let {
            event.value = Event.LaunchWebBrowser(it)
        }
    }

    class Factory(
            private val volumeId: String,
            application: Application
    ): ViewModelProvider.NewInstanceFactory() {

        @Inject lateinit var volumesRepo: VolumesRepo
        @Inject lateinit var application: Application

        init {
            application.appComponent.inject(this)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return VolumeDetailViewModel(volumeId, application, volumesRepo, AndroidSchedulers.mainThread(), Schedulers.computation()) as T
        }

    }

}