package net.olewinski.themoviedbbrowser.data.sources.base

import androidx.lifecycle.LiveData
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

/**
 * Base class for [PageKeyedDataSource]s that uses [Long] value to identify given page and return
 * paged list of [MovieData].
 *
 * @param theMovieDbBrowserDatabase Local database
 * @param coroutineScope            The [CoroutineScope] to perform network operations in.
 */
abstract class BaseMoviesListDataSource<ResponseType>(
    theMovieDbBrowserDatabase: TheMovieDbBrowserDatabase,
    private val coroutineScope: CoroutineScope
) : PageKeyedDataSource<Long, MovieData>() {

    /**
     * Observable [HashSet] containing IDs of favourite movies, backed by Room database's live query.
     */
    private val favouritesMoviesIds = Transformations.map(
        theMovieDbBrowserDatabase.getFavouritesDao().getAllFavouritesMoviesIds()
    ) { favouritesMoviesIdsList ->
        favouritesMoviesIdsList.toHashSet()
    }

    /**
     * [DataLoadingState] representing state of initial movies' data fetch. The differentiation
     * between initial and not-initial fetch might be used in UI.
     */
    val initialNetworkDataLoadingState = MutableLiveData<DataLoadingState>()
    /**
     * [DataLoadingState] representing of further (not initial) movies' data fetch. The
     * differentiation between initial and not-initial fetch might be used in UI.
     */
    val networkDataLoadingState = MutableLiveData<DataLoadingState>()

    private var retryOperation: (() -> Any)? = null

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Long, MovieData>
    ) {
        initialNetworkDataLoadingState.postValue(DataLoadingState.LOADING)

        // Launching request in IO Thread
        coroutineScope.launch(Dispatchers.IO) {
            try {
                // Fetching 1st page of data
                val response = getResponse(1L)

                if (response.isSuccessful) {
                    retryOperation = null

                    val results = getMoviesListFromResponse(response)
                    val nextKey = getTotalPagesNumberFromResponse(response)?.let { totalPages ->
                        // 2nd page should be next, if it exists
                        if (totalPages > 1L) 2L else null
                    }

                    // Enhance fresh data from network with favourites data stored locally
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

        // Launching request in IO Thread
        coroutineScope.launch(Dispatchers.IO) {
            try {
                // Fetching requested page of data
                val response = getResponse(params.key)

                if (response.isSuccessful) {
                    retryOperation = null

                    val results = getMoviesListFromResponse(response)
                    val nextKey = getTotalPagesNumberFromResponse(response)?.let { totalPages ->
                        // Calculating next page's index, if this page exists
                        if (params.key == totalPages) null else params.key + 1L
                    }

                    // Enhance fresh data from network with favourites data stored locally
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
        // Do nothing, we always start from the beginning and only append data; no need to load
        // "previous" data at any moment
    }

    fun retryAllFailed() {
        val previousRetryOperation = retryOperation

        retryOperation = null

        previousRetryOperation?.invoke()
    }

    /**
     * Enhances all passed [MovieData]s with favourites data from local storage. Each favourites
     * data are passed to [MovieData] objects as observable [LiveData] items so it is automatically
     * updated when status is changed in local storage.
     *
     * @param results   [List] of [MovieData] to enhance. Each element of the [List] will be
     *                  enhanced.
     */
    private suspend fun enhanceResultsWithFavouriteData(results: List<MovieData>) =
        withContext(Dispatchers.Default) {
            results.forEach { movieData: MovieData ->
                // Each movie requires it's own LiveData object representing it's individual status
                // stored in database. However, to conserve resources, we don't create individual
                // database query to get specific LiveData object for each movie. That would led to
                // have same amount of observable queries as we have movies on the list, which would
                // be extremely inefficient in case of large data sets. Instead, we just perform 1
                // single observable query to database to obtain the observable list of all
                // favourites movies IDs. Then we use transformation to map this 1 observable query
                // into individual LiveData objects for each movie. As a result, all LiveData
                // objects that describe favourite status inside of all objects representing movies
                // are backed by 1 simple observable database query. Mapping operation is also
                // efficient since it only calls contains method on HashSet.
                movieData.favouriteStatus =
                    Transformations.map(favouritesMoviesIds) { favouritesMoviesIdsSet ->
                        // It's efficient operation, as it's performed on HashSet
                        favouritesMoviesIdsSet.contains(movieData.id)
                    }
            }
        }

    /**
     * Gets [Response] of given type for requested page number.
     *
     * @param page  Index of requested page.
     *
     * @return      [Response] of given type containing data for given page number.
     */
    abstract suspend fun getResponse(page: Long): Response<ResponseType>

    /**
     * Extracts [List] of [MovieData] from given [Response] of given type.
     *
     * @param response  [Response] of given type.
     *
     * @return          [List] of [MovieData] extracted from given [Response] of given type.
     */
    abstract fun getMoviesListFromResponse(response: Response<ResponseType>): List<MovieData>

    /**
     * Extracts total number of pages from given [Response] of given type.
     *
     * @param response  [Response] of given type.
     *
     * @return          Total number of pages extracted from given [Response] of given type. Might
     *                  be null if this information is unavailable.
     */
    abstract fun getTotalPagesNumberFromResponse(response: Response<ResponseType>): Long?
}
