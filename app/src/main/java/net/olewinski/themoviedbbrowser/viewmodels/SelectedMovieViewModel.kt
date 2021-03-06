package net.olewinski.themoviedbbrowser.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import net.olewinski.themoviedbbrowser.data.models.MovieData
import net.olewinski.themoviedbbrowser.data.repository.MoviesRepository

/**
 * View model for passing information between screens about movie selected for showing details.
 *
 * @param moviesRepository  [MoviesRepository]
 */
class SelectedMovieViewModel(private val moviesRepository: MoviesRepository) : ViewModel() {

    private val mutableSelectedMovie = MutableLiveData<MovieData>()

    /**
     * Observable selected movie.
     */
    val selectedMovie: LiveData<MovieData> = mutableSelectedMovie

    /**
     * Selects movie which details should be shown.
     *
     * @param movieData [MovieData] representing selected movie.
     */
    fun selectMovie(movieData: MovieData) {
        mutableSelectedMovie.value = movieData
    }

    /**
     * Handles clicking favourite toggle for selected movie.
     */
    fun onItemFavouriteToggleClicked() {
        selectedMovie.value?.let { selectedMovie ->
            moviesRepository.toggleFavouritesStatusForMovie(GlobalScope, selectedMovie)
        }
    }
}
