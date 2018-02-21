package com.gjsalot.books.data.repo

import com.gjsalot.books.data.api.GoodreadsApi
import com.gjsalot.books.data.api.GoogleBooksApi
import com.gjsalot.books.data.api.model.JsonBookReviewStats
import com.gjsalot.books.data.api.model.JsonVolumeDetailed
import com.gjsalot.books.data.api.model.JsonVolumesResponse
import com.gjsalot.books.data.repo.model.VolumeDetail
import com.gjsalot.books.utils.Optional
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

open class VolumesRepo constructor(
        private val googleBooksApi: GoogleBooksApi,
        private val goodreadsApi: GoodreadsApi
) {

    companion object {
        const val VOLUMES_FIELDS = "totalItems,items(id,volumeInfo(title,imageLinks/thumbnail))"
        const val PAGE_SIZE = 40
    }

    open fun queryVolumes(query: String, offset: Int): Single<JsonVolumesResponse> =
            googleBooksApi
                    .queryVolumes(query, VOLUMES_FIELDS, PAGE_SIZE, offset)
                    .subscribeOn(Schedulers.io())

    open fun volume(volumeId: String): Single<VolumeDetail> {
        fun isbn(volume: JsonVolumeDetailed) =
                volume.volumeInfo.industryIdentifiers?.firstOrNull {
                    it.type.toUpperCase().contains("ISBN_10")
                }?.identifier

        val volumeFlowable = googleBooksApi
                .volume(volumeId)
                .subscribeOn(Schedulers.io())
                .cache()
                .observeOn(Schedulers.computation())

        val ratingFlowable = volumeFlowable
                .flatMap {
                    isbn(it)?.let { isbn ->
                        goodreadsApi
                                .reviewStats(isbn)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.computation())
                                .map { Optional(it.books.firstOrNull()) }
                                .onErrorReturnItem(Optional(null))
                    } ?:
                        Single.just(Optional<JsonBookReviewStats>(null))
                }
                .observeOn(Schedulers.computation())


        return Single.zip(
                volumeFlowable,
                ratingFlowable,
                BiFunction<JsonVolumeDetailed, Optional<JsonBookReviewStats>, VolumeDetail> { volume, bookReviewStats ->
                    val abeBooksUrl = isbn(volume)?.let { isbn ->
                        "https://www.abebooks.com/servlet/SearchResults?sts=t&isbn=$isbn"
                    }

                    VolumeDetail(
                            volume.volumeInfo.imageLinks?.thumbnail,
                            volume.volumeInfo.title,
                            volume.volumeInfo.authors,
                            volume.volumeInfo.publisher,
                            volume.volumeInfo.publishedDate,
                            volume.volumeInfo.pageCount,
                            volume.volumeInfo.description,
                            bookReviewStats.value?.average_rating?.toFloatOrNull(),
                            bookReviewStats.value?.ratings_count,
                            abeBooksUrl
                    )
                }
        )
    }

}