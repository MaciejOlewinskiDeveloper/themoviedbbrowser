package net.olewinski.themoviedbbrowser.data.sources

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PageKeyedDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.olewinski.themoviedbbrowser.cloud.DataLoadingState
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.data.db.TheMovieDbBrowserDatabase
import net.olewinski.themoviedbbrowser.data.models.MovieData
import java.util.*

class SearchMoviesDataSource(
    private val tmdbService: TmdbService,
    theMovieDbBrowserDatabase: TheMovieDbBrowserDatabase,
    private val coroutineScope: CoroutineScope,
    private val searchQuery: String
) : PageKeyedDataSource<Long, MovieData>() {

    private val favouritesMoviesIds = Transformations.map(theMovieDbBrowserDatabase.getFavouritesDao().getAllFavouritesMoviesIds()) { favouritesDataList ->
        favouritesDataList.toHashSet()
    }

    val initialNetworkDataLoadingState = MutableLiveData<DataLoadingState>()
    val networkDataLoadingState = MutableLiveData<DataLoadingState>()

    private var retryOperation: (() -> Any)? = null

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Long, MovieData>
    ) {
        initialNetworkDataLoadingState.postValue(DataLoadingState.LOADING)

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val response = tmdbService.searchMovies(
                    apiKey = TmdbService.TMDB_API_KEY,
                    language = Locale.getDefault().language,
                    query = searchQuery,
                    page = 1L
                )

                if (response.isSuccessful) {
                    retryOperation = null

                    val results = response.body()?.results ?: emptyList()
                    val nextKey = response.body()?.totalPages?.let { totalPages ->
                        if (totalPages > 1L) 2L else null
                    }

                    enhanceResultsWithFavouriteData(results)

                    initialNetworkDataLoadingState.postValue(DataLoadingState.SUCCESS)
                    networkDataLoadingState.postValue(DataLoadingState.SUCCESS)

                    callback.onResult(results, null, nextKey)
                } else {
                    retryOperation = {
                        loadInitial(params, callback)
                    }

                    initialNetworkDataLoadingState.postValue(DataLoadingState.error(response.message()))
                    networkDataLoadingState.postValue(DataLoadingState.error(response.message()))
                }
            } catch (e: Exception) {
                retryOperation = {
                    loadInitial(params, callback)
                }

                initialNetworkDataLoadingState.postValue(DataLoadingState.error(e.message))
                networkDataLoadingState.postValue(DataLoadingState.error(e.message))
            }
        }
    }

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Long, MovieData>) {
        networkDataLoadingState.postValue(DataLoadingState.LOADING)

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val response = tmdbService.getNowPlaying(
                    apiKey = TmdbService.TMDB_API_KEY,
                    language = Locale.getDefault().language,
                    page = params.key
                )

                if (response.isSuccessful) {
                    retryOperation = null

                    val results = response.body()?.results ?: emptyList()
                    val nextKey = response.body()?.totalPages?.let { totalPages ->
                        if (params.key == totalPages) null else params.key + 1L
                    }

                    enhanceResultsWithFavouriteData(results)

                    networkDataLoadingState.postValue(DataLoadingState.SUCCESS)

                    callback.onResult(results, nextKey)
                } else {
                    retryOperation = {
                        loadAfter(params, callback)
                    }

                    networkDataLoadingState.postValue(DataLoadingState.error(response.message()))
                }
            } catch (e: Exception) {
                retryOperation = {
                    loadAfter(params, callback)
                }

                networkDataLoadingState.postValue(DataLoadingState.error(e.message))
            }
        }
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, MovieData>) {
        // Do nothing
    }

    fun retryAllFailed() {
        val previousRetryOperation = retryOperation

        retryOperation = null

        previousRetryOperation?.let { operation ->
            coroutineScope.launch(Dispatchers.IO) {
                operation.invoke()
            }
        }
    }

    private suspend fun enhanceResultsWithFavouriteData(results: List<MovieData>) = withContext(Dispatchers.IO) {
        results.forEach { movieData: MovieData ->
            movieData.favouriteStatus = Transformations.map(favouritesMoviesIds) { favouritesMoviesIdsSet ->
                favouritesMoviesIdsSet.contains(movieData.id)
            }
        }
    }
}
