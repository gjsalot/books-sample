package com.gjsalot.books.data.api

import com.gjsalot.books.data.api.model.JsonBooksReviewStatsResponse
import io.reactivex.Single
import retrofit2.http.*

interface GoodreadsApi {

    @GET("book/review_counts.json")
    fun reviewStats(
            @Query("isbns") isbns: String
    ): Single<JsonBooksReviewStatsResponse>

}