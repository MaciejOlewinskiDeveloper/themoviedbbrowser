package net.olewinski.themoviedbbrowser.data.sources

import kotlinx.coroutines.CoroutineScope
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.data.db.TheMovieDbBrowserDatabase
import net.olewinski.themoviedbbrowser.data.sources.base.BaseMoviesListDataSourceFactory

class NowPlayingDataSourceFactory(
    private val tmdbService: TmdbService,
    private val theMovieDbBrowserDatabase: TheMovieDbBrowserDatabase,
    private val coroutineScope: CoroutineScope
) : BaseMoviesListDataSourceFactory<NowPlayingDataSource>() {
    override fun getBaseMoviesListDataSource() =
        NowPlayingDataSource(tmdbService, theMovieDbBrowserDatabase, coroutineScope)
}
