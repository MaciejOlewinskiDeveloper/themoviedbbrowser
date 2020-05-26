package net.olewinski.themoviedbbrowser.di.modules

import dagger.Module
import dagger.Provides
import net.olewinski.themoviedbbrowser.data.repository.NowPlayingRepository
import net.olewinski.themoviedbbrowser.viewmodels.NowPlayingViewModel

@Module
class NowPlayingFragmentModule {
    @Provides
    fun provideNowPlayingViewModelFactory(nowPlayingRepository: NowPlayingRepository) =
        NowPlayingViewModel.NowPlayingViewModelFactory(nowPlayingRepository)
}
