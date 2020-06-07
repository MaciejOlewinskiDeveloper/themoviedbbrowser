package net.olewinski.themoviedbbrowser.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.olewinski.themoviedbbrowser.data.models.NowPlaying
import net.olewinski.themoviedbbrowser.data.repository.NowPlayingRepository

class SelectedMovieViewModel(private val nowPlayingRepository: NowPlayingRepository) : ViewModel() {

    private val mutableSelectedMovie = MutableLiveData<NowPlaying>()
    val selectedMovie: LiveData<NowPlaying> = mutableSelectedMovie

    fun selectMovie(nowPlaying: NowPlaying) {
        mutableSelectedMovie.value = nowPlaying
    }

    fun onItemFavouriteToggleClicked() {
        selectedMovie.value?.let { selectedMovie ->
            GlobalScope.launch {
                nowPlayingRepository.toggleFavouriteData(selectedMovie)
            }
        }
    }

    class SelectedMovieViewModelFactory(private val nowPlayingRepository: NowPlayingRepository) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SelectedMovieViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SelectedMovieViewModel(nowPlayingRepository) as T
            } else {
                throw Error("Incorrect ViewModel requested: only SelectedMovieViewModel can be provided here")
            }
        }
    }
}
