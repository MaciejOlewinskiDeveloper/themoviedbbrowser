package net.olewinski.themoviedbbrowser.di.modules

import dagger.Module
import dagger.Provides
import net.olewinski.themoviedbbrowser.data.repository.MovieSearchRepository
import net.olewinski.themoviedbbrowser.data.repository.NowPlayingRepository
import net.olewinski.themoviedbbrowser.viewmodels.NowPlayingViewModel
import net.olewinski.themoviedbbrowser.viewmodels.SelectedMovieViewModel

@Module
class FragmentsModule {
    @Provides
    fun provideNowPlayingViewModelFactory(
        nowPlayingRepository: NowPlayingRepository,
        movieSearchRepository: MovieSearchRepository
    ) =
        NowPlayingViewModel.NowPlayingViewModelFactory(nowPlayingRepository, movieSearchRepository)

    @Provides
    fun provideSelectedMovieViewModelFactory(nowPlayingRepository: NowPlayingRepository) =
        SelectedMovieViewModel.SelectedMovieViewModelFactory(nowPlayingRepository)
}
