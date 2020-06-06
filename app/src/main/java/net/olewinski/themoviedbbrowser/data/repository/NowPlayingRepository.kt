package net.olewinski.themoviedbbrowser.data.repository

import androidx.lifecycle.Transformations
import androidx.paging.toLiveData
import kotlinx.coroutines.CoroutineScope
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.data.PagedDataContainer
import net.olewinski.themoviedbbrowser.data.db.TheMovieDbBrowserDatabase
import net.olewinski.themoviedbbrowser.data.models.NowPlaying
import net.olewinski.themoviedbbrowser.data.sources.NowPlayingDataSourceFactory
import net.olewinski.themoviedbbrowser.data.sources.SearchMoviesDataSourceFactory
import net.olewinski.themoviedbbrowser.di.scopes.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class NowPlayingRepository @Inject constructor(
    private val tmdbService: TmdbService,
    private val theMovieDbBrowserDatabase: TheMovieDbBrowserDatabase
) {
    suspend fun toggleFavouriteData(nowPlaying: NowPlaying) {
        theMovieDbBrowserDatabase.getFavouritesDataDao().toggleDataPresence(nowPlaying.id)
    }

    fun getNowPlayingData(coroutineScope: CoroutineScope): PagedDataContainer<NowPlaying> {
        val nowPlayingDataSourceFactory = NowPlayingDataSourceFactory(tmdbService, theMovieDbBrowserDatabase, coroutineScope)

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

    fun searchMovies(coroutineScope: CoroutineScope, searchQuery: String): PagedDataContainer<NowPlaying> {
        val searchMoviesDataSourceFactory = SearchMoviesDataSourceFactory(tmdbService, theMovieDbBrowserDatabase, coroutineScope, searchQuery)

        return PagedDataContainer(
            pagedData = searchMoviesDataSourceFactory.toLiveData(32),
            networkState = Transformations.switchMap(searchMoviesDataSourceFactory.searchMoviesDataSource) { searchMoviesDataSource ->
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
}
