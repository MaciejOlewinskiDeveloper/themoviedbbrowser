package net.olewinski.themoviedbbrowser.data.sources

import kotlinx.coroutines.CoroutineScope
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.data.db.TheMovieDbBrowserDatabase
import net.olewinski.themoviedbbrowser.data.sources.base.BaseMoviesListDataSourceFactory

class SearchMoviesDataSourceFactory(
    private val tmdbService: TmdbService,
    private val theMovieDbBrowserDatabase: TheMovieDbBrowserDatabase,
    private val coroutineScope: CoroutineScope,
    private val searchQuery: String
) : BaseMoviesListDataSourceFactory<SearchMoviesDataSource>() {
    override fun getBaseMoviesListDataSource() =
        SearchMoviesDataSource(tmdbService, theMovieDbBrowserDatabase, coroutineScope, searchQuery)
}
