package net.olewinski.themoviedbbrowser.cloud.service

import net.olewinski.themoviedbbrowser.cloud.models.NowPlayingResults
import net.olewinski.themoviedbbrowser.cloud.models.SearchMovieResults
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbService {
    companion object {
        const val TMDB_ENDPOINT = "https://api.themoviedb.org/3/"
        const val TMDB_API_KEY = "5db23d1d4c860bb729176569f5d28780"

        private const val IMAGES_BASE_URL = "https://image.tmdb.org/t/p/"

        const val SMALL_POSTER_BASE_URL = "$IMAGES_BASE_URL/w154/"
    }

    @GET("movie/now_playing")
    suspend fun getNowPlaying(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Long,
        @Query("region") region: String? = null
    ): Response<NowPlayingResults>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("query") query: String,
        @Query("page") page: Long,
        @Query("region") region: String? = null
    ): Response<SearchMovieResults>
}
