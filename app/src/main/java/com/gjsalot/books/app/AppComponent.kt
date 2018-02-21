package com.gjsalot.books.app

import com.gjsalot.books.data.api.ApiModule
import com.gjsalot.books.data.repo.RepoModule
import com.gjsalot.books.ui.volume_detail.VolumeDetailViewModel
import com.gjsalot.books.ui.volumes_list.VolumesListViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
        AppModule::class,
        ApiModule::class,
        RepoModule::class
])
interface AppComponent {

    fun inject(factory: VolumesListViewModel.Factory)
    fun inject(factory: VolumeDetailViewModel.Factory)

}