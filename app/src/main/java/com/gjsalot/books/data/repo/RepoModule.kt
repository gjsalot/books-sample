package com.gjsalot.books.data.repo

import com.gjsalot.books.data.api.GoodreadsApi
import com.gjsalot.books.data.api.GoogleBooksApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class RepoModule {

    @Provides
    @Singleton
    open fun provideVolumesRepo(
            googleBooksApi: GoogleBooksApi,
            goodreadsApi: GoodreadsApi
    ) =
            VolumesRepo(googleBooksApi, goodreadsApi)

}
