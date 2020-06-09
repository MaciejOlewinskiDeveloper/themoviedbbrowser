package net.olewinski.themoviedbbrowser.data.sources

import kotlinx.coroutines.CoroutineScope
import net.olewinski.themoviedbbrowser.cloud.models.NowPlayingResults
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.data.db.TheMovieDbBrowserDatabase
import net.olewinski.themoviedbbrowser.data.sources.base.BaseMoviesListDataSource
import retrofit2.Response
import java.util.*

/**
 * Data source that provides paged data of "Now Playing" movies.
 *
 * @param tmdbService               Object for accessing TMBD web service API.
 * @param theMovieDbBrowserDatabase Local database.
 * @param coroutineScope            [CoroutineScope] to run data fetching operations inside.
 */
class NowPlayingDataSource(
    private val tmdbService: TmdbService,
    theMovieDbBrowserDatabase: TheMovieDbBrowserDatabase,
    coroutineScope: CoroutineScope
) : BaseMoviesListDataSource<NowPlayingResults>(theMovieDbBrowserDatabase, coroutineScope) {

    override suspend fun getResponse(page: Long) = tmdbService.getNowPlaying(
        apiKey = TmdbService.TMDB_API_KEY,
        language = Locale.getDefault().language,
        page = page
    )

    override fun getMoviesListFromResponse(response: Response<NowPlayingResults>) =
        response.body()?.results ?: emptyList()

    override fun getTotalPagesNumberFromResponse(response: Response<NowPlayingResults>) =
        response.body()?.totalPages
}
