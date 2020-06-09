package net.olewinski.themoviedbbrowser.data.sources

import kotlinx.coroutines.CoroutineScope
import net.olewinski.themoviedbbrowser.cloud.models.SearchMovieResults
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.data.db.TheMovieDbBrowserDatabase
import net.olewinski.themoviedbbrowser.data.sources.base.BaseMoviesListDataSource
import retrofit2.Response
import java.util.*

/**
 * Data source that provides paged data of movies that are results of search against given query.
 *
 * @param tmdbService               Object for accessing TMBD web service API.
 * @param theMovieDbBrowserDatabase Local database.
 * @param coroutineScope            [CoroutineScope] to run data fetching operations inside.
 * @param searchQuery               Query to search movies against.
 */
class SearchMoviesDataSource(
    private val tmdbService: TmdbService,
    theMovieDbBrowserDatabase: TheMovieDbBrowserDatabase,
    coroutineScope: CoroutineScope,
    private val searchQuery: String
) : BaseMoviesListDataSource<SearchMovieResults>(theMovieDbBrowserDatabase, coroutineScope) {

    override suspend fun getResponse(page: Long) = tmdbService.searchMovies(
        apiKey = TmdbService.TMDB_API_KEY,
        language = Locale.getDefault().language,
        query = searchQuery,
        page = page
    )

    override fun getMoviesListFromResponse(response: Response<SearchMovieResults>) =
        response.body()?.results ?: emptyList()

    override fun getTotalPagesNumberFromResponse(response: Response<SearchMovieResults>) =
        response.body()?.totalPages
}
