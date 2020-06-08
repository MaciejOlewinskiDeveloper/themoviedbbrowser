package net.olewinski.themoviedbbrowser.data.sources.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PageKeyedDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.olewinski.themoviedbbrowser.cloud.DataLoadingState
import net.olewinski.themoviedbbrowser.data.db.TheMovieDbBrowserDatabase
import net.olewinski.themoviedbbrowser.data.models.MovieData
import retrofit2.Response

abstract class BaseMoviesListDataSource<ResponseType>(
    theMovieDbBrowserDatabase: TheMovieDbBrowserDatabase,
    private val coroutineScope: CoroutineScope
) : PageKeyedDataSource<Long, MovieData>() {

    private val favouritesMoviesIds = Transformations.map(
        theMovieDbBrowserDatabase.getFavouritesDao().getAllFavouritesMoviesIds()
    ) { favouritesDataList ->
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
                val response = getResponse(1L)

                if (response.isSuccessful) {
                    retryOperation = null

                    val results = getMoviesListFromResponse(response)
                    val nextKey = getTotalPagesNumberFromResponse(response)?.let { totalPages ->
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
                val response = getResponse(params.key)

                if (response.isSuccessful) {
                    retryOperation = null

                    val results = getMoviesListFromResponse(response)
                    val nextKey = getTotalPagesNumberFromResponse(response)?.let { totalPages ->
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

    private suspend fun enhanceResultsWithFavouriteData(results: List<MovieData>) =
        withContext(Dispatchers.IO) {
            results.forEach { movieData: MovieData ->
                movieData.favouriteStatus =
                    Transformations.map(favouritesMoviesIds) { favouritesMoviesIdsSet ->
                        favouritesMoviesIdsSet.contains(movieData.id)
                    }
            }
        }

    abstract suspend fun getResponse(page: Long): Response<ResponseType>

    abstract fun getMoviesListFromResponse(response: Response<ResponseType>): List<MovieData>

    abstract fun getTotalPagesNumberFromResponse(response: Response<ResponseType>): Long?
}