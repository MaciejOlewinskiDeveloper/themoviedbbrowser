package net.olewinski.themoviedbbrowser.viewmodels

import android.app.SearchManager
import android.database.Cursor
import android.database.MatrixCursor
import android.provider.BaseColumns
import androidx.lifecycle.*
import androidx.paging.PagedList
import kotlinx.coroutines.*
import net.olewinski.themoviedbbrowser.cloud.DataLoadingState
import net.olewinski.themoviedbbrowser.data.models.MovieData
import net.olewinski.themoviedbbrowser.data.repository.MoviesRepository
import net.olewinski.themoviedbbrowser.util.OneTimeEvent

const val AUTOCOMPLETE_INPUT_LENGTH_MINIMUM_THRESHOLD = 2

/**
 * Helper class representing navigation request; to be used to request navigation actions from view
 * that observes current view model.
 */
sealed class NavigationRequest

/**
 * Request to navigate to movie details screen.
 *
 * @param movieData Movie which details should be shown.
 */
class MovieDetailsNavigationRequest(val movieData: MovieData) : NavigationRequest()

/**
 * View model for movies list screen
 *
 * @param moviesRepository  [MoviesRepository]
 */
class MoviesListViewModel(private val moviesRepository: MoviesRepository) : ViewModel() {

    private val mutableNavigationRequest = MutableLiveData<OneTimeEvent<NavigationRequest>>()

    /**
     * Observable navigation request.
     */
    val navigationRequest: LiveData<OneTimeEvent<NavigationRequest>> = mutableNavigationRequest

    private val mutableSearchSuggestions = MutableLiveData<Cursor>()

    /**
     * Observable search suggestions.
     */
    val searchSuggestions: LiveData<Cursor> = mutableSearchSuggestions

    private var fetchSearchSuggestionsOperation: Job? = null

    private val currentSearchQuery = MutableLiveData<String?>().apply {
        value = null
    }

    /**
     * Last typed search query.
     */
    var lastTypedSearchQuery: String? = null

    private val moviesData = Transformations.map(currentSearchQuery) { searchQuery ->
        if (searchQuery.isNullOrBlank()) {
            moviesRepository.getNowPlayingData(viewModelScope)
        } else {
            moviesRepository.searchMovies(viewModelScope, searchQuery)
        }
    }

    /**
     * Movies list data.
     */
    val pagedMoviesData: LiveData<PagedList<MovieData>> =
        Transformations.switchMap(moviesData) { value ->
            value.pagedData
        }

    /**
     * Network data loading state for non-initial loading.
     */
    val networkState: LiveData<DataLoadingState> = Transformations.switchMap(moviesData) { value ->
        value.state
    }

    /**
     * Network data loading state for initial loading (refreshing).
     */
    val refreshState: LiveData<DataLoadingState> = Transformations.switchMap(moviesData) { value ->
        value.refreshState
    }

    /**
     * Triggers data refresh.
     */
    fun refreshMoviesData() {
        moviesData.value?.refreshDataOperation?.invoke()
    }

    /**
     * Requests updating search suggestions for passed search query.
     *
     * @param searchQuery   Query for which the search suggestions should be fetched.
     */
    fun requestSearchSuggestionsUpdate(searchQuery: String?) {
        lastTypedSearchQuery = searchQuery

        // Cleaning previous search suggestions by setting empty cursor
        mutableSearchSuggestions.value =
            MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1), 0)

        // Stopping any pending fetch suggestions operation
        fetchSearchSuggestionsOperation?.cancel()
        fetchSearchSuggestionsOperation = null

        // Only fetching suggestions for search queries of minimum length
        if (!searchQuery.isNullOrBlank() && searchQuery.length > AUTOCOMPLETE_INPUT_LENGTH_MINIMUM_THRESHOLD) {
            fetchSearchSuggestionsOperation = viewModelScope.launch {
                val newSearchSuggestions = moviesRepository.getMoviesSearchSuggestions(searchQuery)

                val matrixCursor = MatrixCursor(
                    arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1),
                    newSearchSuggestions.size
                )

                // Computation thread for creating matrix cursor
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

    /**
     * Requests searching movies by given query
     *
     * @param searchQuery Search query.
     */
    fun searchMovies(searchQuery: String?) {
        lastTypedSearchQuery = searchQuery
        currentSearchQuery.value = searchQuery
    }

    /**
     * Requests "Now Playing" data
     */
    fun getNowPlaying() {
        lastTypedSearchQuery = null
        currentSearchQuery.value = null
    }

    /**
     * Handles [MovieData] item click.
     *
     * @param movieData Clicked [MovieData].
     */
    fun onItemClicked(movieData: MovieData) {
        mutableNavigationRequest.value = OneTimeEvent(MovieDetailsNavigationRequest(movieData))
    }

    /**
     * Handles clicking favourite toggle for movie.
     *
     * @param movieData [MovieData] which favourite toggle was clicked.
     */
    fun onItemFavouriteToggleClicked(movieData: MovieData) {
        moviesRepository.toggleFavouritesStatusForMovie(GlobalScope, movieData)
    }

    /**
     * Request retry of last failed data fetch operation.
     */
    fun retry() {
        moviesData.value?.retryOperation?.invoke()
    }
}
