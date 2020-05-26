package net.olewinski.themoviedbbrowser.di.components

import android.content.Context
import dagger.Component
import net.olewinski.themoviedbbrowser.di.modules.ContextModule
import net.olewinski.themoviedbbrowser.di.modules.NowPlayingFragmentModule
import net.olewinski.themoviedbbrowser.di.modules.TmdbServiceModule
import net.olewinski.themoviedbbrowser.di.qualifiers.ApplicationContext
import net.olewinski.themoviedbbrowser.di.scopes.ApplicationScope
import net.olewinski.themoviedbbrowser.viewmodels.NowPlayingViewModel

@ApplicationScope
@Component(modules = [ContextModule::class, TmdbServiceModule::class, NowPlayingFragmentModule::class])
interface ApplicationComponent {
    @ApplicationContext
    fun getApplicationContext(): Context

    fun getNowPlayingViewModelFactory(): NowPlayingViewModel.NowPlayingViewModelFactory
}
