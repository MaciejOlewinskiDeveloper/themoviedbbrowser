package net.olewinski.themoviedbbrowser.data.sources

import kotlinx.coroutines.CoroutineScope
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.data.db.TheMovieDbBrowserDatabase
import net.olewinski.themoviedbbrowser.data.sources.base.BaseMoviesListDataSourceFactory

/**
 * Factory for [SearchMoviesDataSource].
 *
 * @param tmdbService               Object for accessing TMBD web service API.
 * @param theMovieDbBrowserDatabase Local database.
 * @param coroutineScope            [CoroutineScope] to run data fetching operations inside.
 * @param searchQuery               Query to search movies against.
 */
class SearchMoviesDataSourceFactory(
    private val tmdbService: TmdbService,
    private val theMovieDbBrowserDatabase: TheMovieDbBrowserDatabase,
    private val coroutineScope: CoroutineScope,
    private val searchQuery: String
) : BaseMoviesListDataSourceFactory<SearchMoviesDataSource>() {

    override fun getBaseMoviesListDataSource() =
        SearchMoviesDataSource(tmdbService, theMovieDbBrowserDatabase, coroutineScope, searchQuery)
}
