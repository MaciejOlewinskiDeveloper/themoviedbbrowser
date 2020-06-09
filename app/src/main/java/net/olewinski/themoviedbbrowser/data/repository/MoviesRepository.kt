package net.olewinski.themoviedbbrowser.data.repository

import android.util.Log
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.data.PagedDataContainer
import net.olewinski.themoviedbbrowser.data.db.TheMovieDbBrowserDatabase
import net.olewinski.themoviedbbrowser.data.models.MovieData
import net.olewinski.themoviedbbrowser.data.sources.NowPlayingDataSourceFactory
import net.olewinski.themoviedbbrowser.data.sources.SearchMoviesDataSourceFactory
import net.olewinski.themoviedbbrowser.data.sources.base.BaseMoviesListDataSourceFactory
import net.olewinski.themoviedbbrowser.di.scopes.ApplicationScope
import java.util.*
import javax.inject.Inject

private const val DEFAULT_PAGE_SIZE_ITEMS = 32

/**
 * Main movies' repository, app's component as described in https://developer.android.com/jetpack/docs/guide
 *
 * @param tmdbService               TMDB's REST web service accessor
 * @param theMovieDbBrowserDatabase Local database
 */
@ApplicationScope
class MoviesRepository @Inject constructor(
    private val tmdbService: TmdbService,
    private val theMovieDbBrowserDatabase: TheMovieDbBrowserDatabase
) {
    companion object {
        private val LOG_TAG = MoviesRepository::class.java.simpleName
    }

    /**
     * Changes current favourite status for given movie.
     *
     * @param coroutineScope    The scope of coroutine (please check [CoroutineScope] documentation)
     *                          in which current favourite status should be changed.
     * @param movieData         Movie that should have favourite status changed.
     */
    fun toggleFavouritesStatusForMovie(coroutineScope: CoroutineScope, movieData: MovieData) =
        coroutineScope.launch {
            theMovieDbBrowserDatabase.getFavouritesDao()
                .toggleFavouritesStatusForMovie(movieData.id)
        }

    /**
     * Returns [PagedDataContainer] representing paged list of movies returned by TMDB's "Now
     * Playing" API together with data loading status metadata.
     *
     * @param coroutineScope    The scope of coroutine (please check [CoroutineScope] documentation)
     *                          in which data fetching operation should be run.
     *
     * @return                  [PagedDataContainer] with "Now Playing" movies with data loading
     *                          status metadata.
     */
    fun getNowPlayingData(coroutineScope: CoroutineScope) = getPagedDataContainer(
        NowPlayingDataSourceFactory(
            tmdbService,
            theMovieDbBrowserDatabase,
            coroutineScope
        )
    )

    /**
     * Returns [PagedDataContainer] representing paged list of movies returned by TMDB's "Search"
     * API for given query, together with data loading status metadata.
     *
     * @param coroutineScope    The scope of coroutine (please check [CoroutineScope] documentation)
     *                          in which data fetching operation should be run.
     * @param searchQuery       Query to search movies against.
     *
     * @return                  [PagedDataContainer] with "Now Playing" movies with data loading
     *                          status metadata.
     */
    fun searchMovies(coroutineScope: CoroutineScope, searchQuery: String) = getPagedDataContainer(
        SearchMoviesDataSourceFactory(
            tmdbService,
            theMovieDbBrowserDatabase,
            coroutineScope,
            searchQuery
        )
    )

    /**
     * Returns search suggestions for given search query.
     *
     * @param searchQuery   Query to fetch search suggestions for.
     *
     * @return              [List] of search suggestions for given query.
     */
    suspend fun getMoviesSearchSuggestions(searchQuery: String): List<String> {
        try {
            val response = tmdbService.searchMovies(
                apiKey = TmdbService.TMDB_API_KEY,
                language = Locale.getDefault().language,
                query = searchQuery,
                page = 1L
            )

            if (response.isSuccessful) {
                val results = response.body()?.results ?: emptyList()
                val autocompleteData = mutableListOf<String>()

                // Moving list operations to computation thread
                return withContext(Dispatchers.Default) {
                    for (result in results) {
                        autocompleteData.add(result.title)
                    }

                    autocompleteData
                }
            }
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Exception while downloading autocomplete data: $e")
        }

        return emptyList()
    }

    /**
     * Helper method for creating [PagedDataContainer] from movies data requested by [BaseMoviesListDataSourceFactory].
     */
    private fun getPagedDataContainer(moviesListDataSourceFactory: BaseMoviesListDataSourceFactory<*>) =
        PagedDataContainer(
            pagedData = moviesListDataSourceFactory.toLiveData(DEFAULT_PAGE_SIZE_ITEMS),
            state = Transformations.switchMap(moviesListDataSourceFactory.moviesListDataSource) { dataSource ->
                dataSource.networkDataLoadingState
            },
            retryOperation = {
                moviesListDataSourceFactory.moviesListDataSource.value?.retryAllFailed()
            },
            refreshDataOperation = {
                moviesListDataSourceFactory.moviesListDataSource.value?.invalidate()
            },
            refreshState = Transformations.switchMap(moviesListDataSourceFactory.moviesListDataSource) { dataSource ->
                dataSource.initialNetworkDataLoadingState
            }
        )
}
