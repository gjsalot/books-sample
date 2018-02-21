package com.gjsalot.books.data.api

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApiModule {

    companion object {
        private const val GOOGLE_BOOKS_NAME = "google_books"
        private const val GOOGLE_BOOKS_BASE_URL = "https://www.googleapis.com/books/v1/"
        private const val GOOGLE_BOOKS_API_KEY = "AIzaSyDUwvNLTbHuvbC1qs1B_K5_KNF6vSotpH0"

        private const val GOODREADS_NAME = "goodreads"
        private const val GOODREADS_BASE_URL = "https://www.goodreads.com/"
        private const val GOODREADS_API_KEY = "1lmCuEN1CeX2kPs9YyUUw"

        private const val AMAZON_NAME = "amazon"
        private const val AMAZON_BASE_URL = "https://www.amazon.com/gp/"
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder().build()
    }

    @Provides
    @Singleton
    fun provideConverterFactory(moshi: Moshi): Converter.Factory {
        return MoshiConverterFactory.create(moshi)
    }

    @Provides
    @Singleton
    fun provideCallAdapterFactory(): CallAdapter.Factory {
        return RxJava2CallAdapterFactory.create()
    }

    //region Google Books

    @Provides
    @Singleton
    @Named(GOOGLE_BOOKS_NAME)
    fun provideGoogleBooksInterceptor() = Interceptor { chain ->
        val original = chain.request()
        val originalHttpUrl = original.url()

        val url = originalHttpUrl.newBuilder()
                .addQueryParameter("key", GOOGLE_BOOKS_API_KEY)
                .build()

        val requestBuilder = original.newBuilder().url(url)
        val request = requestBuilder.build()

        chain.proceed(request)
    }

    @Provides
    @Singleton
    @Named(GOOGLE_BOOKS_NAME)
    fun provideGoogleBooksOkHttpClient(@Named(GOOGLE_BOOKS_NAME) interceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()
    }

    @Provides
    @Singleton
    @Named(GOOGLE_BOOKS_NAME)
    fun provideGoogleBooksRetrofit(converterFactory: Converter.Factory,
                        callAdapterFactory: CallAdapter.Factory,
                        @Named(GOOGLE_BOOKS_NAME) okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .baseUrl(GOOGLE_BOOKS_BASE_URL)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .client(okHttpClient)
                .build()
    }

    @Provides
    @Singleton
    fun provideGoogleBooksApi(@Named(GOOGLE_BOOKS_NAME) retrofit: Retrofit): GoogleBooksApi {
        return retrofit.create(GoogleBooksApi::class.java)
    }

    //endregion

    //region Goodreads

    @Provides
    @Singleton
    @Named(GOODREADS_NAME)
    fun provideGoodreadsInterceptor() = Interceptor { chain ->
        val original = chain.request()
        val originalHttpUrl = original.url()

        val url = originalHttpUrl.newBuilder()
                .addQueryParameter("key", GOODREADS_API_KEY)
                .build()

        val requestBuilder = original.newBuilder().url(url)
        val request = requestBuilder.build()

        chain.proceed(request)
    }

    @Provides
    @Singleton
    @Named(GOODREADS_NAME)
    fun provideGoodreadsOkHttpClient(@Named(GOODREADS_NAME) interceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()
    }

    @Provides
    @Singleton
    @Named(GOODREADS_NAME)
    fun provideGoodreadsRetrofit(converterFactory: Converter.Factory,
                                 callAdapterFactory: CallAdapter.Factory,
                                 @Named(GOODREADS_NAME) okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .baseUrl(GOODREADS_BASE_URL)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .client(okHttpClient)
                .build()
    }

    @Provides
    @Singleton
    fun provideGoodreadsApi(@Named(GOODREADS_NAME) retrofit: Retrofit): GoodreadsApi {
        return retrofit.create(GoodreadsApi::class.java)
    }

    //endregion

}
