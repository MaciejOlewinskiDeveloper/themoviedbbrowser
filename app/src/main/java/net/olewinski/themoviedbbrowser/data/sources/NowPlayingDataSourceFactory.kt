package net.olewinski.themoviedbbrowser.data.sources

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import kotlinx.coroutines.CoroutineScope
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.data.db.TheMovieDbBrowserDatabase
import net.olewinski.themoviedbbrowser.data.models.NowPlaying

class NowPlayingDataSourceFactory(
    private val tmdbService: TmdbService,
    private val theMovieDbBrowserDatabase: TheMovieDbBrowserDatabase,
    private val coroutineScope: CoroutineScope
) : DataSource.Factory<Long, NowPlaying>() {

    private val mutableNowPlayingDataSource = MutableLiveData<NowPlayingDataSource>()
    val nowPlayingDataSource: LiveData<NowPlayingDataSource> = mutableNowPlayingDataSource

    override fun create(): DataSource<Long, NowPlaying> {
        val nowPlayingDataSourceInstance = NowPlayingDataSource(tmdbService, theMovieDbBrowserDatabase, coroutineScope)

        mutableNowPlayingDataSource.postValue(nowPlayingDataSourceInstance)

        return nowPlayingDataSourceInstance
    }
}
