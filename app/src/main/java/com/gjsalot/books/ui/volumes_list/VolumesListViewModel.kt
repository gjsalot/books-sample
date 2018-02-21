package com.gjsalot.books.ui.volumes_list

import android.arch.lifecycle.*
import android.support.v7.util.DiffUtil
import com.gjsalot.books.app.Application
import com.gjsalot.books.data.api.model.JsonVolume
import com.gjsalot.books.data.api.model.JsonVolumesResponse
import com.gjsalot.books.data.repo.VolumesRepo
import com.gjsalot.books.utils.SingleLiveEvent
import com.gjsalot.books.utils.plusAssign
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class VolumesListViewModel(
        private val volumesRepo: VolumesRepo,
        application: Application,
        private val computation: Scheduler,
        private val mainThread: Scheduler
): AndroidViewModel(application),
        VolumesListAdapter.Listener {

    data class Volume(
            val id: String,
            val title: String,
            val thumbnail: String?
    )

    data class ViewState(
        val volumes: List<Volume>,
        val diffResult: DiffUtil.DiffResult?
    )

    sealed class Event {
        data class ShowVolumeDetail(val volumeId: String): Event()
    }

    private data class LoadCommand(
            val query: String,
            val offset: Int
    )

    private data class LoadResult(
            val nextPage: List<Volume>,
            val isFirstPage: Boolean
    )

    private val disposable = CompositeDisposable()
    private val queryBehaviourSubject = BehaviorSubject.createDefault("")
    private val loadCommandBehaviourSubject = BehaviorSubject.createDefault(LoadCommand("", 0))
    private val offsetSubject = BehaviorSubject.createDefault(0)
    private val volumesSubject = BehaviorSubject.createDefault(emptyList<Volume>())

    private val viewState = MutableLiveData<ViewState>()
    fun viewState(): LiveData<ViewState> = viewState

    private val event = SingleLiveEvent<Event>()
    fun event(): LiveData<Event> = event

    init {
        // Issue a load command when the query changes with offset 0
        disposable += queryBehaviourSubject
                .toFlowable(BackpressureStrategy.LATEST)
                .debounce(200, TimeUnit.MILLISECONDS, computation)
                .observeOn(mainThread)
                .subscribe { query ->
                    loadCommandBehaviourSubject.onNext(LoadCommand(query, 0))
                }

        // Issue a load command when a new offset is requested, distinct by query/offset
        disposable += offsetSubject
                .toFlowable(BackpressureStrategy.BUFFER)
                .distinctUntilChanged()
                .observeOn(mainThread)
                .subscribe { offset ->
                    val query = loadCommandBehaviourSubject.value.query
                    loadCommandBehaviourSubject.onNext(LoadCommand(query, offset))
                }

        // Handle load commands by fetching next page, and either replacing the list or appending to
        // it. This keeps giant list copies off the ui thread.
        disposable += loadCommandBehaviourSubject
                .toFlowable(BackpressureStrategy.BUFFER)
                .observeOn(computation)
                .switchMap { (query, offset) ->
                    val nextPageFlowable =
                            if (query.isNotBlank())
                                volumesRepo
                                        .queryVolumes(query, offset)
                                        .toFlowable()
                                        .observeOn(computation)
                                        .onErrorReturnItem(JsonVolumesResponse(0, emptyList()))
                            else
                                Flowable.just(JsonVolumesResponse(0, emptyList())).observeOn(computation)

                    nextPageFlowable.map { nextPageResponse ->
                        val nextPage = nextPageResponse.items?.map {
                            Volume(it.id, it.volumeInfo.title, it.volumeInfo.imageLinks?.thumbnail)
                        } ?: emptyList()

                        LoadResult(nextPage, offset == 0)
                    }
                }
                .observeOn(computation)
                .subscribe({ (nextPage, isFirstPage) ->
                    if (isFirstPage)
                        volumesSubject.onNext(nextPage)
                    else
                        volumesSubject.onNext(volumesSubject.value + nextPage)
                })

        // Update the ViewState every time a new immutable list of volumes is available to be shown.
        // Create a diffResult to update an adapter that has the previous list.
        val initialViewState = ViewState(emptyList(), null)
        disposable += volumesSubject
                .toFlowable(BackpressureStrategy.BUFFER)
                .observeOn(computation)
                .map { volumes ->
                    ViewState(volumes, null)
                }
                .scan(initialViewState, { prev, next ->
                    val diffResult = DiffUtil.calculateDiff(object: DiffUtil.Callback() {

                        override fun getOldListSize() = prev.volumes.size

                        override fun getNewListSize() = next.volumes.size

                        override fun areItemsTheSame(prevI: Int, nextI: Int) =
                                prev.volumes[prevI].id == next.volumes[nextI].id

                        override fun areContentsTheSame(prevI: Int, nextI: Int) =
                                prev.volumes[prevI] == next.volumes[nextI]

                    })

                    next.copy(diffResult = diffResult)
                })
                .observeOn(mainThread)
                .subscribe {
                    viewState.value = it
                }
    }

    fun onSearchQueryChanged(query: String) {
        queryBehaviourSubject.onNext(query)
    }

    //region VolumesListAdapter.Listener

    override fun onScrolledNearBottom() {
        loadCommandBehaviourSubject.onNext(loadCommandBehaviourSubject.value.copy(offset = viewState.value!!.volumes.size))
    }

    override fun onVolumeClicked(volume: Volume) {
        event.value = Event.ShowVolumeDetail(volume.id)
    }

    //endregion

    override fun onCleared() {
        disposable.clear()
    }

    class Factory(
            application: Application
    ): ViewModelProvider.NewInstanceFactory() {

        @Inject lateinit var volumesRepo: VolumesRepo
        @Inject lateinit var application: Application

        init {
            application.appComponent.inject(this)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return VolumesListViewModel(volumesRepo, application, Schedulers.computation(), AndroidSchedulers.mainThread()) as T
        }

    }

}