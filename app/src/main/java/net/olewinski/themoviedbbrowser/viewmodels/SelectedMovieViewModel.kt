package net.olewinski.themoviedbbrowser.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.GlobalScope
import net.olewinski.themoviedbbrowser.data.models.MovieData
import net.olewinski.themoviedbbrowser.data.repository.MoviesRepository

class SelectedMovieViewModel(private val moviesRepository: MoviesRepository) : ViewModel() {

    private val mutableSelectedMovie = MutableLiveData<MovieData>()
    val selectedMovie: LiveData<MovieData> = mutableSelectedMovie

    fun selectMovie(movieData: MovieData) {
        mutableSelectedMovie.value = movieData
    }

    fun onItemFavouriteToggleClicked() {
        selectedMovie.value?.let { selectedMovie ->
            moviesRepository.toggleFavouritesStatusForMovie(GlobalScope, selectedMovie)
        }
    }

    class SelectedMovieViewModelFactory(private val moviesRepository: MoviesRepository) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SelectedMovieViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SelectedMovieViewModel(moviesRepository) as T
            } else {
                throw Error("Incorrect ViewModel requested: only SelectedMovieViewModel can be provided here")
            }
        }
    }
}
