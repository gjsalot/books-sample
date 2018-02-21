package com.gjsalot.books.data.api

import com.gjsalot.books.data.api.model.JsonVolumeDetailed
import com.gjsalot.books.data.api.model.JsonVolumesResponse
import io.reactivex.Single
import retrofit2.http.*

interface GoogleBooksApi {

    @GET("volumes")
    fun queryVolumes(
            @Query("q") query: String,
            @Query("fields") fields: String,
            @Query("maxResults") maxResults: Int,
            @Query("startIndex") startIndex: Int
    ): Single<JsonVolumesResponse>

    @GET("volumes/{volumeId}")
    fun volume(
            @Path("volumeId") volumeId: String
    ): Single<JsonVolumeDetailed>

}