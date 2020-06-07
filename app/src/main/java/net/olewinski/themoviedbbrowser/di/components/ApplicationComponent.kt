package net.olewinski.themoviedbbrowser.di.components

import android.content.Context
import dagger.Component
import net.olewinski.themoviedbbrowser.di.modules.ContextModule
import net.olewinski.themoviedbbrowser.di.modules.DatabaseModule
import net.olewinski.themoviedbbrowser.di.modules.FragmentsModule
import net.olewinski.themoviedbbrowser.di.modules.TmdbServiceModule
import net.olewinski.themoviedbbrowser.di.qualifiers.ApplicationContext
import net.olewinski.themoviedbbrowser.di.scopes.ApplicationScope
import net.olewinski.themoviedbbrowser.viewmodels.NowPlayingViewModel
import net.olewinski.themoviedbbrowser.viewmodels.SelectedMovieViewModel

@ApplicationScope
@Component(modules = [ContextModule::class, TmdbServiceModule::class, FragmentsModule::class, DatabaseModule::class])
interface ApplicationComponent {
    @ApplicationContext
    fun getApplicationContext(): Context

    fun getNowPlayingViewModelFactory(): NowPlayingViewModel.NowPlayingViewModelFactory

    fun getSelectedMovieViewModelFactory(): SelectedMovieViewModel.SelectedMovieViewModelFactory
}
