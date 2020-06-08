package net.olewinski.themoviedbbrowser.data.repository

import android.util.Log
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.data.PagedDataContainer
import net.olewinski.themoviedbbrowser.data.db.TheMovieDbBrowserDatabase
import net.olewinski.themoviedbbrowser.data.models.MovieData
import net.olewinski.themoviedbbrowser.data.sources.NowPlayingDataSourceFactory
import net.olewinski.themoviedbbrowser.data.sources.SearchMoviesDataSourceFactory
import net.olewinski.themoviedbbrowser.di.scopes.ApplicationScope
import java.util.*
import javax.inject.Inject

private const val DEFAULT_PAGE_SIZE_ITEMS = 32

@ApplicationScope
class MoviesRepository @Inject constructor(
    private val tmdbService: TmdbService,
    private val theMovieDbBrowserDatabase: TheMovieDbBrowserDatabase
) {
    companion object {
        private val LOG_TAG = MoviesRepository::class.java.simpleName
    }

    suspend fun toggleFavouriteData(movieData: MovieData) {
        theMovieDbBrowserDatabase.getFavouritesDao().toggleFavouritesStatusForMovie(movieData.id)
    }

    fun getNowPlayingData(coroutineScope: CoroutineScope): PagedDataContainer<MovieData> {
        val nowPlayingDataSourceFactory = NowPlayingDataSourceFactory(tmdbService, theMovieDbBrowserDatabase, coroutineScope)

        return PagedDataContainer(
            pagedData = nowPlayingDataSourceFactory.toLiveData(DEFAULT_PAGE_SIZE_ITEMS),
            state = Transformations.switchMap(nowPlayingDataSourceFactory.nowPlayingDataSource) { nowPlayingDataSource ->
                nowPlayingDataSource.networkDataLoadingState
            },
            retryOperation = {
                nowPlayingDataSourceFactory.nowPlayingDataSource.value?.retryAllFailed()
            },
            refreshDataOperation = {
                nowPlayingDataSourceFactory.nowPlayingDataSource.value?.invalidate()
            },
            refreshState = Transformations.switchMap(nowPlayingDataSourceFactory.nowPlayingDataSource) { nowPlayingDataSource ->
                nowPlayingDataSource.initialNetworkDataLoadingState
            }
        )
    }

    fun searchMovies(coroutineScope: CoroutineScope, searchQuery: String): PagedDataContainer<MovieData> {
        val searchMoviesDataSourceFactory = SearchMoviesDataSourceFactory(tmdbService, theMovieDbBrowserDatabase, coroutineScope, searchQuery)

        return PagedDataContainer(
            pagedData = searchMoviesDataSourceFactory.toLiveData(DEFAULT_PAGE_SIZE_ITEMS),
            state = Transformations.switchMap(searchMoviesDataSourceFactory.searchMoviesDataSource) { searchMoviesDataSource ->
                searchMoviesDataSource.networkDataLoadingState
            },
            retryOperation = {
                searchMoviesDataSourceFactory.searchMoviesDataSource.value?.retryAllFailed()
            },
            refreshDataOperation = {
                searchMoviesDataSourceFactory.searchMoviesDataSource.value?.invalidate()
            },
            refreshState = Transformations.switchMap(searchMoviesDataSourceFactory.searchMoviesDataSource) { searchMoviesDataSource ->
                searchMoviesDataSource.initialNetworkDataLoadingState
            }
        )
    }

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
}
