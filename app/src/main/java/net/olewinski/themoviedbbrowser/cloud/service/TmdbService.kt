package net.olewinski.themoviedbbrowser.cloud.service

import net.olewinski.themoviedbbrowser.cloud.models.NowPlayingResults
import net.olewinski.themoviedbbrowser.cloud.models.SearchMovieResults
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface for Retrofit library representing TMDB REST Web Service API; used for creating API-access object
 */
interface TmdbService {
    companion object {
        /**
         * URL's base for using main endpoint of TMDB API
         */
        const val TMDB_ENDPOINT = "https://api.themoviedb.org/3/"

        /**
         * App's specific TMDB API key
         */
        const val TMDB_API_KEY = "5db23d1d4c860bb729176569f5d28780"

        /**
         * URL's base for downloading images from TMDB
         */
        private const val IMAGES_BASE_URL = "https://image.tmdb.org/t/p/"

        /**
         * Middle part of URL for downloading small movie's posters images
         */
        const val SMALL_POSTER_BASE_URL = "$IMAGES_BASE_URL/w154/"

        /**
         * Middle part of URL for downloading medium movie's backdrops images
         */
        const val MEDIUM_BACKDROP_BASE_URL = "$IMAGES_BASE_URL/w780/"
    }

    /**
     * Returns [Response] with one page of data from TMDB's "Now Playing" API
     *
     * @param apiKey    App's specific TMDB API key
     * @param language  ISO 639-1 language code
     * @param page      Number of requested page with data
     * @param region    ISO 3166-1 region code
     *
     * @return          [Response] of [NowPlayingResults]
     */
    @GET("movie/now_playing")
    suspend fun getNowPlaying(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Long,
        @Query("region") region: String? = null
    ): Response<NowPlayingResults>

    /**
     * Returns [Response] with one page of data from TMDB's "Search Movies" API
     *
     * @param apiKey    App's specific TMDB API key
     * @param language  ISO 639-1 language code
     * @param query     Search query
     * @param page      Number of requested page with data
     * @param region    ISO 3166-1 region code
     *
     * @return          [Response] of [SearchMovieResults]
     */
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("query") query: String,
        @Query("page") page: Long,
        @Query("region") region: String? = null
    ): Response<SearchMovieResults>
}
