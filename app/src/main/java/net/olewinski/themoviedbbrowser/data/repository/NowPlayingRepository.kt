package net.olewinski.themoviedbbrowser.data.repository

import androidx.lifecycle.Transformations
import androidx.paging.toLiveData
import kotlinx.coroutines.CoroutineScope
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.data.PagedDataContainer
import net.olewinski.themoviedbbrowser.data.models.NowPlaying
import net.olewinski.themoviedbbrowser.data.sources.NowPlayingDataSourceFactory
import net.olewinski.themoviedbbrowser.di.scopes.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class NowPlayingRepository @Inject constructor(private val tmdbService: TmdbService) {

    fun getNowPlayingData(coroutineScope: CoroutineScope): PagedDataContainer<NowPlaying> {
        val nowPlayingDataSourceFactory = NowPlayingDataSourceFactory(tmdbService, coroutineScope)

        return PagedDataContainer(
            pagedData = nowPlayingDataSourceFactory.toLiveData(32),
            networkState = Transformations.switchMap(nowPlayingDataSourceFactory.nowPlayingDataSource) { nowPlayingDataSource ->
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
}
