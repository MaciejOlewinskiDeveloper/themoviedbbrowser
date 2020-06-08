package net.olewinski.themoviedbbrowser.cloud.models

import com.google.gson.annotations.SerializedName
import net.olewinski.themoviedbbrowser.data.models.MovieData

data class NowPlayingResults(
    @SerializedName("page")
    val page: Long,

    @SerializedName("total_pages")
    val totalPages: Long,

    @SerializedName("results")
    val results: List<MovieData>,

    @SerializedName("total_results")
    val totalResults: Long
)
