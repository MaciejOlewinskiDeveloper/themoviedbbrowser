package net.olewinski.themoviedbbrowser.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.data.db.TheMovieDbBrowserDatabase
import net.olewinski.themoviedbbrowser.di.scopes.ApplicationScope
import java.util.*
import javax.inject.Inject

@ApplicationScope
class MovieSearchRepository @Inject constructor(
    private val tmdbService: TmdbService,
    private val theMovieDbBrowserDatabase: TheMovieDbBrowserDatabase
) {
    companion object {
        private val LOG_TAG = MovieSearchRepository::class.java.simpleName
    }

    suspend fun getSearchSuggestions(query: String): List<String> {
        try {
            val response = tmdbService.searchMovies(
                apiKey = TmdbService.TMDB_API_KEY,
                language = Locale.getDefault().language,
                query = query,
                page = 1L
            )

            if (response.isSuccessful) {
                val results = response.body()?.results ?: emptyList()
                val autocompleteData = mutableListOf<String>()

                return withContext(Dispatchers.Default) {
                    for (result in results) {
                        autocompleteData.add(result.title)
                    }

                    autocompleteData
                }
            }
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Exception while downloading autocomplete data: $e")
        }

        return emptyList()
    }
}
