package net.olewinski.themoviedbbrowser.cloud.models

import com.google.gson.annotations.SerializedName
import net.olewinski.themoviedbbrowser.data.models.NowPlaying

data class NowPlayingResults(
    @SerializedName("page")
    val page: Long,

    @SerializedName("results")
    val results: List<NowPlaying>,

    @SerializedName("total_pages")
    val totalPages: Long,

    @SerializedName("total_results")
    val totalResults: Long
)
