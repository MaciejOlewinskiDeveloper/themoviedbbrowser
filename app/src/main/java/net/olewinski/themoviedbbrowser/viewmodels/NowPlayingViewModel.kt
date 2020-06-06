package net.olewinski.themoviedbbrowser.viewmodels

import android.app.SearchManager
import android.database.Cursor
import android.database.MatrixCursor
import android.provider.BaseColumns
import androidx.lifecycle.*
import kotlinx.coroutines.*
import net.olewinski.themoviedbbrowser.data.models.NowPlaying
import net.olewinski.themoviedbbrowser.data.repository.MovieSearchRepository
import net.olewinski.themoviedbbrowser.data.repository.NowPlayingRepository

const val AUTOCOMPLETE_INPUT_LENGTH_MINIMUM_THRESHOLD = 2

class NowPlayingViewModel(
    private val nowPlayingRepository: NowPlayingRepository,
    private val movieSearchRepository: MovieSearchRepository
) : ViewModel() {
    private val mutableSearchSuggestions = MutableLiveData<Cursor>()
    val searchSuggestions: LiveData<Cursor> = mutableSearchSuggestions

    private var fetchSearchSuggestionsOperation: Job? = null

    private val currentSearchQuery = MutableLiveData<String?>().apply {
        value = null
    }

    private val moviesData = Transformations.map(currentSearchQuery) { searchQuery ->
        if (searchQuery.isNullOrBlank()) {
            nowPlayingRepository.getNowPlayingData(viewModelScope)
        } else {
            nowPlayingRepository.searchMovies(viewModelScope, searchQuery)
        }
    }

    val pagedData = Transformations.switchMap(moviesData) { value ->
        value.pagedData
    }

    val networkState = Transformations.switchMap(moviesData) { value ->
        value.networkState
    }

    val refreshState = Transformations.switchMap(moviesData) { value ->
        value.refreshState
    }

    fun refresh() {
        moviesData.value?.refreshDataOperation?.invoke()
    }

    fun requestSearchSuggestionsUpdate(searchQuery: String?) {
        // Cleaning previous search suggestions by setting empty cursor
        mutableSearchSuggestions.value =
            MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1), 0)

        // Stopping any pending fetch suggestions operation
        fetchSearchSuggestionsOperation?.cancel()
        fetchSearchSuggestionsOperation = null

        if (!searchQuery.isNullOrBlank() && searchQuery.length > AUTOCOMPLETE_INPUT_LENGTH_MINIMUM_THRESHOLD) {
            fetchSearchSuggestionsOperation = viewModelScope.launch {
                val newSearchSuggestions = movieSearchRepository.getSearchSuggestions(searchQuery)

                val matrixCursor = MatrixCursor(
                    arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1),
                    newSearchSuggestions.size
                )

                // Computation
                withContext(Dispatchers.Default) {
                    newSearchSuggestions.forEachIndexed { id, searchSuggestion ->
                        matrixCursor.newRow()
                            .add(BaseColumns._ID, id)
                            .add(SearchManager.SUGGEST_COLUMN_TEXT_1, searchSuggestion)
                    }
                }

                mutableSearchSuggestions.value = matrixCursor
            }
        }
    }

    fun searchMovies(searchQuery: String?) {
        currentSearchQuery.value = searchQuery
    }

    fun showNowPlaying() {
        if (currentSearchQuery.value != null) {
            currentSearchQuery.value = null
        }
    }

    fun onItemFavouriteToggleClicked(nowPlaying: NowPlaying) {
        GlobalScope.launch {
            nowPlayingRepository.toggleFavouriteData(nowPlaying)
        }
    }

    class NowPlayingViewModelFactory(
        private val nowPlayingRepository: NowPlayingRepository,
        private val movieSearchRepository: MovieSearchRepository
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NowPlayingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NowPlayingViewModel(nowPlayingRepository, movieSearchRepository) as T
            } else {
                throw Error("Incorrect ViewModel requested: only NowPlayingViewModel can be provided here")
            }
        }
    }
}
