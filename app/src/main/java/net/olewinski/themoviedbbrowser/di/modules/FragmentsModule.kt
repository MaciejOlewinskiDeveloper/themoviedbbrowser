package net.olewinski.themoviedbbrowser.di.modules

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import net.olewinski.themoviedbbrowser.data.repository.MoviesRepository
import net.olewinski.themoviedbbrowser.viewmodels.MoviesListViewModel
import net.olewinski.themoviedbbrowser.viewmodels.SelectedMovieViewModel

/**
 * Dagger [Module] providing factories for [ViewModel]s.
 */
@Module
class FragmentsModule {
    @Provides
    fun provideMoviesListViewModelFactory(moviesRepository: MoviesRepository) =
        MoviesListViewModel.Factory(moviesRepository)

    @Provides
    fun provideSelectedMovieViewModelFactory(moviesRepository: MoviesRepository) =
        SelectedMovieViewModel.Factory(moviesRepository)
}
