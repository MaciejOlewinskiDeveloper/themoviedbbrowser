package net.olewinski.themoviedbbrowser.data.sources

import kotlinx.coroutines.CoroutineScope
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.data.db.TheMovieDbBrowserDatabase
import net.olewinski.themoviedbbrowser.data.sources.base.BaseMoviesListDataSourceFactory

/**
 * Factory for [NowPlayingDataSource].
 *
 * @param tmdbService               Object for accessing TMBD web service API.
 * @param theMovieDbBrowserDatabase Local database.
 * @param coroutineScope            [CoroutineScope] to run data fetching operations inside.
 */
class NowPlayingDataSourceFactory(
    private val tmdbService: TmdbService,
    private val theMovieDbBrowserDatabase: TheMovieDbBrowserDatabase,
    private val coroutineScope: CoroutineScope
) : BaseMoviesListDataSourceFactory<NowPlayingDataSource>() {

    override fun getBaseMoviesListDataSource() =
        NowPlayingDataSource(tmdbService, theMovieDbBrowserDatabase, coroutineScope)
}
