package net.olewinski.themoviedbbrowser.data.sources

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PageKeyedDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.olewinski.themoviedbbrowser.cloud.NetworkDataLoadingState
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.data.db.TheMovieDbBrowserDatabase
import net.olewinski.themoviedbbrowser.data.models.NowPlaying
import java.util.*

class SearchMoviesDataSource(
    private val tmdbService: TmdbService,
    theMovieDbBrowserDatabase: TheMovieDbBrowserDatabase,
    private val coroutineScope: CoroutineScope,
    private val searchQuery: String
) : PageKeyedDataSource<Long, NowPlaying>() {

    private val favouritesMoviesIds = Transformations.map(theMovieDbBrowserDatabase.getFavouritesDataDao().getAllFavouritesMoviesIds()) { favouritesDataList ->
        favouritesDataList.toHashSet()
    }

    val initialNetworkDataLoadingState = MutableLiveData<NetworkDataLoadingState>()
    val networkDataLoadingState = MutableLiveData<NetworkDataLoadingState>()

    private var retryOperation: (() -> Any)? = null

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Long, NowPlaying>
    ) {
        initialNetworkDataLoadingState.postValue(NetworkDataLoadingState.LOADING)
        networkDataLoadingState.postValue(NetworkDataLoadingState.LOADING)

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

                    initialNetworkDataLoadingState.postValue(NetworkDataLoadingState.READY)
                    networkDataLoadingState.postValue(NetworkDataLoadingState.READY)

                    callback.onResult(results, null, nextKey)
                } else {
                    retryOperation = {
                        loadInitial(params, callback)
                    }

                    initialNetworkDataLoadingState.postValue(NetworkDataLoadingState.error(response.message()))
                    networkDataLoadingState.postValue(NetworkDataLoadingState.error(response.message()))
                }
            } catch (e: Exception) {
                retryOperation = {
                    loadInitial(params, callback)
                }

                initialNetworkDataLoadingState.postValue(NetworkDataLoadingState.error(e.message))
                networkDataLoadingState.postValue(NetworkDataLoadingState.error(e.message))
            }
        }
    }

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Long, NowPlaying>) {
        networkDataLoadingState.postValue(NetworkDataLoadingState.LOADING)

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

                    networkDataLoadingState.postValue(NetworkDataLoadingState.READY)

                    callback.onResult(results, nextKey)
                } else {
                    retryOperation = {
                        loadAfter(params, callback)
                    }

                    networkDataLoadingState.postValue(NetworkDataLoadingState.error(response.message()))
                }
            } catch (e: Exception) {
                retryOperation = {
                    loadAfter(params, callback)
                }

                networkDataLoadingState.postValue(NetworkDataLoadingState.error(e.message))
            }
        }
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, NowPlaying>) {
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

    private suspend fun enhanceResultsWithFavouriteData(results: List<NowPlaying>) = withContext(Dispatchers.IO) {
        results.forEach { nowPlaying: NowPlaying ->
            nowPlaying.favouriteStatus = Transformations.map(favouritesMoviesIds) { favouritesMoviesIdsSet ->
                favouritesMoviesIdsSet.contains(nowPlaying.id)
            }
        }
    }
}
