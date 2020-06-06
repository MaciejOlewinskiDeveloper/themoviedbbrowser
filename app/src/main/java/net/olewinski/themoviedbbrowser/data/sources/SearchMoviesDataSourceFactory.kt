package net.olewinski.themoviedbbrowser.data.sources

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import kotlinx.coroutines.CoroutineScope
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.data.models.NowPlaying

class SearchMoviesDataSourceFactory(
    private val tmdbService: TmdbService,
    private val coroutineScope: CoroutineScope,
    private val searchQuery: String
) : DataSource.Factory<Long, NowPlaying>() {

    private val mutableSearchMoviesDataSource = MutableLiveData<SearchMoviesDataSource>()
    val searchMoviesDataSource: LiveData<SearchMoviesDataSource> = mutableSearchMoviesDataSource

    override fun create(): DataSource<Long, NowPlaying> {
        val searchMoviesDataSourceInstance = SearchMoviesDataSource(tmdbService, coroutineScope, searchQuery)

        mutableSearchMoviesDataSource.postValue(searchMoviesDataSourceInstance)

        return searchMoviesDataSourceInstance
    }
}
