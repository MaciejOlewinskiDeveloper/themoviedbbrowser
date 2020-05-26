package net.olewinski.themoviedbbrowser.data.sources

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.olewinski.themoviedbbrowser.cloud.NetworkDataLoadingState
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.data.models.NowPlaying
import java.util.*

class NowPlayingDataSource(
    private val tmdbService: TmdbService,
    private val coroutineScope: CoroutineScope
) : PageKeyedDataSource<Long, NowPlaying>() {

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
                val response = tmdbService.getNowPlaying(
                    apiKey = TmdbService.TMDB_API_KEY,
                    language = Locale.getDefault().language,
                    page = 1L
                )

                if (response.isSuccessful) {
                    retryOperation = null

                    val results = response.body()?.results ?: emptyList()
                    val nextKey = response.body()?.totalPages?.let { totalPages ->
                        if (totalPages > 1L) 2L else null
                    }

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

        previousRetryOperation?.let { previousRetryOperation ->
            coroutineScope.launch(Dispatchers.IO) {
                previousRetryOperation.invoke()
            }
        }
    }
}
