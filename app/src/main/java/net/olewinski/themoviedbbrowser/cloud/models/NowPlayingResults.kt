package net.olewinski.themoviedbbrowser.cloud.models

import com.google.gson.annotations.SerializedName
import net.olewinski.themoviedbbrowser.data.models.MovieData

/**
 * Model for network response from TMDB "Now Playing" API; represents one page of data.
 */
data class NowPlayingResults(
    /**
     * Number of page that is represented by the response
     */
    @SerializedName("page")
    val page: Long,

    /**
     * Total number of available pages of data
     */
    @SerializedName("total_pages")
    val totalPages: Long,

    /**
     * Movies data
     */
    @SerializedName("results")
    val results: List<MovieData>,

    /**
     * Total number of results available on server
     */
    @SerializedName("total_results")
    val totalResults: Long
)
