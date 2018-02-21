package com.gjsalot.books.app

import com.gjsalot.books.data.api.ApiModule
import com.gjsalot.books.data.repo.RepoModule

class Application: android.app.Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .apiModule(ApiModule())
                .repoModule(RepoModule())
                .build()
    }

}