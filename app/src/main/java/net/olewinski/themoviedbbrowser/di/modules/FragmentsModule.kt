package net.olewinski.themoviedbbrowser.di.modules

import dagger.Module
import dagger.Provides
import net.olewinski.themoviedbbrowser.data.repository.MovieSearchRepository
import net.olewinski.themoviedbbrowser.data.repository.MoviesRepository
import net.olewinski.themoviedbbrowser.viewmodels.MoviesListViewModel
import net.olewinski.themoviedbbrowser.viewmodels.SelectedMovieViewModel

@Module
class FragmentsModule {
    @Provides
    fun provideMoviesListViewModelFactory(
        moviesRepository: MoviesRepository,
        movieSearchRepository: MovieSearchRepository
    ) = MoviesListViewModel.MoviesListViewModelFactory(moviesRepository, movieSearchRepository)

    @Provides
    fun provideSelectedMovieViewModelFactory(moviesRepository: MoviesRepository) =
        SelectedMovieViewModel.SelectedMovieViewModelFactory(moviesRepository)
}
