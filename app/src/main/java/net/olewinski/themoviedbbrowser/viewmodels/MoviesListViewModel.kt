package net.olewinski.themoviedbbrowser.viewmodels

import android.app.SearchManager
import android.database.Cursor
import android.database.MatrixCursor
import android.provider.BaseColumns
import androidx.lifecycle.*
import kotlinx.coroutines.*
import net.olewinski.themoviedbbrowser.data.models.MovieData
import net.olewinski.themoviedbbrowser.data.repository.MoviesRepository
import net.olewinski.themoviedbbrowser.util.OneTimeEvent

const val AUTOCOMPLETE_INPUT_LENGTH_MINIMUM_THRESHOLD = 2

sealed class NavigationRequest
class MovieDetailsNavigationRequest(val movieData: MovieData) : NavigationRequest()

class MoviesListViewModel(private val moviesRepository: MoviesRepository) : ViewModel() {
    private val mutableNavigationRequest = MutableLiveData<OneTimeEvent<NavigationRequest>>()
    val navigationRequest: LiveData<OneTimeEvent<NavigationRequest>> = mutableNavigationRequest

    private val mutableSearchSuggestions = MutableLiveData<Cursor>()
    val searchSuggestions: LiveData<Cursor> = mutableSearchSuggestions

    private var fetchSearchSuggestionsOperation: Job? = null

    private val currentSearchQuery = MutableLiveData<String?>().apply {
        value = null
    }

    var lastTypedSearchQuery: String? = null

    private val moviesData = Transformations.map(currentSearchQuery) { searchQuery ->
        if (searchQuery.isNullOrBlank()) {
            moviesRepository.getNowPlayingData(viewModelScope)
        } else {
            moviesRepository.searchMovies(viewModelScope, searchQuery)
        }
    }

    val pagedData = Transformations.switchMap(moviesData) { value ->
        value.pagedData
    }

    val networkState = Transformations.switchMap(moviesData) { value ->
        value.state
    }

    val refreshState = Transformations.switchMap(moviesData) { value ->
        value.refreshState
    }

    fun refresh() {
        moviesData.value?.refreshDataOperation?.invoke()
    }

    fun requestSearchSuggestionsUpdate(searchQuery: String?) {
        lastTypedSearchQuery = searchQuery

        // Cleaning previous search suggestions by setting empty cursor
        mutableSearchSuggestions.value =
            MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1), 0)

        // Stopping any pending fetch suggestions operation
        fetchSearchSuggestionsOperation?.cancel()
        fetchSearchSuggestionsOperation = null

        if (!searchQuery.isNullOrBlank() && searchQuery.length > AUTOCOMPLETE_INPUT_LENGTH_MINIMUM_THRESHOLD) {
            fetchSearchSuggestionsOperation = viewModelScope.launch {
                val newSearchSuggestions = moviesRepository.getMoviesSearchSuggestions(searchQuery)

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
        lastTypedSearchQuery = searchQuery
        currentSearchQuery.value = searchQuery
    }

    fun showNowPlaying() {
        lastTypedSearchQuery = null
        currentSearchQuery.value = null
    }

    fun onItemClicked(movieData: MovieData) {
        mutableNavigationRequest.value = OneTimeEvent(MovieDetailsNavigationRequest(movieData))
    }

    fun onItemFavouriteToggleClicked(movieData: MovieData) {
        GlobalScope.launch {
            moviesRepository.toggleFavouriteData(movieData)
        }
    }

    fun retry() {
        moviesData.value?.retryOperation?.invoke()
    }

    class MoviesListViewModelFactory(private val moviesRepository: MoviesRepository) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MoviesListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MoviesListViewModel(moviesRepository) as T
            } else {
                throw Error("Incorrect ViewModel requested: only MoviesListViewModel can be provided here")
            }
        }
    }
}
